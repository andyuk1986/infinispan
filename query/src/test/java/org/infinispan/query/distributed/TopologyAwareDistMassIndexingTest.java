package org.infinispan.query.distributed;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.fwk.TestCacheManagerFactory;

/**
 * Tests verifying that Mass Indexer works properly on Topology Aware nodes.
 */
public class TopologyAwareDistMassIndexingTest extends DistributedMassIndexingTest {

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);
      builder.indexing()
            .enable()
            .indexLocalOnly(false)
            .addProperty("hibernate.search.default.indexmanager", "org.infinispan.query.indexmanager.InfinispanIndexManager")
            .addProperty("default.directory_provider", "infinispan")
            .addProperty("hibernate.search.default.exclusive_index_use", "false")
            .addProperty("lucene_version", "LUCENE_36");

      for(int i = 0; i < NUM_NODES; i++) {

         GlobalConfigurationBuilder globalConfigurationBuilder = GlobalConfigurationBuilder
               .defaultClusteredBuilder();
         globalConfigurationBuilder.transport().machineId("a" + i).rackId("b" + i).siteId("test" + i);

         EmbeddedCacheManager cm1 = TestCacheManagerFactory.createClusteredCacheManager(
               globalConfigurationBuilder, builder);
         cm1.defineConfiguration("Cache", builder.build());
         cacheManagers.add(cm1);

         caches.add(cm1.getCache("Cache"));
      }

      waitForClusterToForm();
   }
}
