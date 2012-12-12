package org.infinispan.distexec;

import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.TopologyAwareAddress;
import org.infinispan.test.fwk.TestCacheManagerFactory;

/**
 * // TODO: Document this
 *
 * @author anna.manukyan
 * @since 4.0
 */
public class DistributedExecutorWithTopologyAwareNodesTest extends DistributedExecutorTest {
   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);

      GlobalConfiguration gc1 = GlobalConfiguration.getClusteredDefault();
      updatedSiteInfo(gc1, "s0", "r0", "m0");
      EmbeddedCacheManager cm1 = TestCacheManagerFactory.createCacheManager(gc1, getDefaultClusteredConfig(Configuration.CacheMode.DIST_SYNC));
      cm1.defineConfiguration(CACHE_NAME, builder.build());

      cacheManagers.add(cm1);

      GlobalConfiguration gc2 = GlobalConfiguration.getClusteredDefault();
      updatedSiteInfo(gc2, "s1", "r1", "m1");
      EmbeddedCacheManager cm2 = TestCacheManagerFactory.createCacheManager(gc2, getDefaultClusteredConfig(Configuration.CacheMode.DIST_SYNC));
      cm1.defineConfiguration(CACHE_NAME, builder.build());

      cacheManagers.add(cm2);

      waitForClusterToForm();
   }

   public void testA() {
      assert address(0) instanceof TopologyAwareAddress;
      assert address(1) instanceof TopologyAwareAddress;
   }

   private void updatedSiteInfo(GlobalConfiguration gc1, String s0, String r0, String m0) {
      gc1.setSiteId(s0);
      gc1.setRackId(r0);
      gc1.setMachineId(m0);
   }
}
