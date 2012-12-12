package org.infinispan.distexec;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.loaders.dummy.DummyInMemoryCacheStore;

/**
 * Tests are added for testing DistributedExecutors with cache loaders.
 */
public class DistributedExecutorWithCacheLoaderTest extends DistributedExecutorTest {

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(getCacheMode(), false);
      builder.eviction().maxEntries(1).strategy(EvictionStrategy.LRU);
      builder.loaders().passivation(true).addStore().cacheStore(new DummyInMemoryCacheStore(getClass().getSimpleName()));
      builder.storeAsBinary().enable();

      createClusteredCaches(2, cacheName(), builder);
   }
}
