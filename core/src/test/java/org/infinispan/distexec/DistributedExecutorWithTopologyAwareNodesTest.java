package org.infinispan.distexec;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

/**
 * Tests are added for verifying the Distributed Executors for Topology Aware Nodes.
 *
 * @author Anna Manukyan
 */
@Test(groups = "functional", testName = "distexec.DistributedExecutorWithTopologyAwareNodesTest")
public class DistributedExecutorWithTopologyAwareNodesTest extends DistributedExecutorTest {

   private String CACHE_NAME = "DistributedExecutorWithTopologyAwareNodesTest";

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder =
            getDefaultClusteredCacheConfig(getCacheMode(), false);

      GlobalConfigurationBuilder globalConfigurationBuilder = GlobalConfigurationBuilder.defaultClusteredBuilder();
      globalConfigurationBuilder.transport().machineId("a").rackId("b").siteId("test1");

      EmbeddedCacheManager cm1 = TestCacheManagerFactory.createClusteredCacheManager(globalConfigurationBuilder,
                                                                                     builder);
      cm1.defineConfiguration(CACHE_NAME, builder.build());
      cacheManagers.add(cm1);

      globalConfigurationBuilder = GlobalConfigurationBuilder.defaultClusteredBuilder();
      globalConfigurationBuilder.transport().machineId("b").rackId("b").siteId("test2");
      EmbeddedCacheManager cm2 = TestCacheManagerFactory.createClusteredCacheManager(globalConfigurationBuilder,
                                                                                     builder);

      cm2.defineConfiguration(CACHE_NAME, builder.build());
      cacheManagers.add(cm2);

      waitForClusterToForm();
   }

   public CacheMode getCacheMode() {
      return CacheMode.DIST_SYNC;
   }
}
