package org.infinispan.server.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.test.Eventually;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.server.test.core.Common;
import org.infinispan.server.test.core.persistence.Database;
import org.infinispan.server.test.junit5.InfinispanServerExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Tests String-based jdbc cache store under the following circumstances:
 * <p>
 * passivation == false --all cache entries should always be also in the cache store
 * preload == true --after server restart, all entries should appear in the cache immediately
 * purge == false --all entries should remain in the cache store after server  * restart
 * <p>
 * Other attributes like singleton, shared, fetch-state do not make sense in single node cluster.
 *
 */
@org.infinispan.server.test.core.tags.Database
public class JdbcStringBasedCacheStoreIT {

    @RegisterExtension
    public static InfinispanServerExtension SERVERS = PersistenceIT.SERVERS;

    @ParameterizedTest
    @ArgumentsSource(Common.DatabaseProvider.class)
    public void testFailover(Database database) throws Exception {
        JdbcConfigurationUtil jdbcUtil = new JdbcConfigurationUtil(CacheMode.REPL_SYNC, database, false, true)
                .setLockingConfigurations();
        RemoteCache<String, String> cache = SERVERS.hotrod()
                .withClientConfiguration(database.getHotrodClientProperties())
                .withServerConfiguration(jdbcUtil.getConfigurationBuilder()).create();
        try(TableManipulation table = new TableManipulation(cache.getName(), jdbcUtil.getPersistenceConfiguration())) {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            // test passivation==false, database should contain all entries which are in the cache
            assertNotNull(table.getValueByKey("k1"));
            assertNotNull(table.getValueByKey("k2"));

            SERVERS.getServerDriver().stopCluster();
            SERVERS.getServerDriver().restartCluster();

            assertNotNull(table.getValueByKey("k1"));
            assertNotNull(table.getValueByKey("k2"));
            assertNull(cache.withFlags(Flag.SKIP_CACHE_LOAD).get("k3"));
            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
            //when the entry is removed from the cache, it should be also removed from the cache store (the store
            //and the cache are the same sets of keys)
            cache.remove("k1");
            assertNull(table.getValueByKey("k1"));
            cache.clear();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(Common.DatabaseProvider.class)
    public void testPreload(Database database) throws Exception {
        JdbcConfigurationUtil jdbcUtil = new JdbcConfigurationUtil(CacheMode.REPL_SYNC, database, false, true)
                .setLockingConfigurations();
        RemoteCache<String, String> cache = SERVERS.hotrod()
                .withClientConfiguration(database.getHotrodClientProperties())
                .withServerConfiguration(jdbcUtil.getConfigurationBuilder()).create();
        try(TableManipulation table = new TableManipulation(cache.getName(), jdbcUtil.getPersistenceConfiguration())) {
            cache.clear();
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            assertNotNull(table.getValueByKey("k1"));
            assertNotNull(table.getValueByKey("k2"));

            SERVERS.getServerDriver().stopCluster();
            SERVERS.getServerDriver().restartCluster();

            // test preload==true, entries should be immediately in the cache after restart
            assertEquals(2, cache.size());
            // test purge==false, entries should remain in the database after restart
            assertNotNull(table.getValueByKey("k1"));
            assertNotNull(table.getValueByKey("k2"));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(Common.DatabaseProvider.class)
    public void testSoftRestartWithPassivation(Database database) throws Exception {
        JdbcConfigurationUtil jdbcUtil = new JdbcConfigurationUtil(CacheMode.REPL_SYNC, database, true, false)
              .setEviction()
              .setLockingConfigurations();
        RemoteCache<String, String> cache = SERVERS.hotrod()
                .withClientConfiguration(database.getHotrodClientProperties())
                .withServerConfiguration(jdbcUtil.getConfigurationBuilder()).create();
        try(TableManipulation table = new TableManipulation(cache.getName(), jdbcUtil.getPersistenceConfiguration())) {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            //not yet in store (eviction.max-entries=2, LRU)
            assertNull(table.getValueByKey("k1"));
            assertNull(table.getValueByKey("k2"));
            cache.put("k3", "v3");
            //now some key is evicted and stored in store
            assertEquals(2, getNumberOfEntriesInMemory(cache));
            //the passivation is asynchronous
            Eventually.eventuallyEquals(1, table::countAllRows);

            SERVERS.getServerDriver().stopCluster();
            SERVERS.getServerDriver().restartCluster(); //soft stop should store all entries from cache to store

            // test preload==false
            assertEquals(0, getNumberOfEntriesInMemory(cache));
            // test purge==false, entries should remain in the database after restart
            assertEquals(3, table.countAllRows());
            assertEquals("v1", cache.get("k1"));
        }
    }

    /**
     * This test differs from the preceding expecting 1 entry in the DB
     * after fail-over instead of 3 when doing soft
     * restart.
     */
    @ParameterizedTest
    @ArgumentsSource(Common.DatabaseProvider.class)
    public void testFailoverWithPassivation(Database database) throws Exception {
        JdbcConfigurationUtil jdbcUtil = new JdbcConfigurationUtil(CacheMode.REPL_SYNC, database, true, false)
              .setEviction()
              .setLockingConfigurations();

        RemoteCache<String, String> cache = SERVERS.hotrod()
                .withClientConfiguration(database.getHotrodClientProperties())
                .withServerConfiguration(jdbcUtil.getConfigurationBuilder()).create();
        try(TableManipulation table = new TableManipulation(cache.getName(), jdbcUtil.getPersistenceConfiguration())) {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            assertNull(table.getValueByKey("k1"));
            assertNull(table.getValueByKey("k2"));
            cache.put("k3", "v3");
            assertEquals(2, getNumberOfEntriesInMemory(cache));
            Eventually.eventuallyEquals(1, table::countAllRows);

            SERVERS.getServerDriver().killCluster();
            SERVERS.getServerDriver().restartCluster();

            assertEquals(0, getNumberOfEntriesInMemory(cache));
            assertEquals(1, table.countAllRows());
            // Eviction may not always remove k1
            List<Map.Entry<String, String>> list = cache.entrySet().stream().toList();
            assertEquals(1, list.size());
            Map.Entry<String, String> entry = list.get(0);
            assertEquals(entry.getValue().substring(1), entry.getKey().substring(1));
        }
    }

    private int getNumberOfEntriesInMemory(RemoteCache<?, ?> cache) {
       return cache.withFlags(Flag.SKIP_CACHE_LOAD).size();
    }
}
