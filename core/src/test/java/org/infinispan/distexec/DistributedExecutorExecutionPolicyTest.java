package org.infinispan.distexec;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.TopologyAwareAddress;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Tests are added for testing execution policy for distributed executors.
 *
 * @author Anna Manukyan
 */
@Test(groups = "functional", testName = "distexec.DistributedExecutorExecutionPolicyTest")
public class DistributedExecutorExecutionPolicyTest extends MultipleCacheManagersTest {

   public static final String CACHE_NAME = "TestCache";

   public DistributedExecutorExecutionPolicyTest() {
      cleanup = CleanupPhase.AFTER_METHOD;
   }

   @Override
   protected void createCacheManagers() throws Throwable {
   }

   private EmbeddedCacheManager createCacheManager(int counter, int siteId, int machineId, int rankId) {
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);

      GlobalConfiguration gc1 = GlobalConfiguration.getClusteredDefault();
      updatedSiteInfo(gc1, "s" + (siteId > 0 ? siteId : counter), "r" + (rankId > 0 ? rankId : counter),
                      "m" + (machineId > 0 ? machineId : counter));
      EmbeddedCacheManager cm1 = TestCacheManagerFactory.createCacheManager(gc1, getDefaultClusteredConfig(
            org.infinispan.config.Configuration.CacheMode.DIST_SYNC));

      cm1.defineConfiguration(CACHE_NAME, builder.build());

      return cm1;
   }

   public void testExecutionPolicyNotSameMachine() throws ExecutionException, InterruptedException {
      for(int i = 1; i <= 2; i++) {
         cacheManagers.add(createCacheManager(i, 0, 0 ,0));
      }

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_MACHINE);
   }

   public void testExecutionPolicySameMachine() throws ExecutionException, InterruptedException {
      cacheManagers.add(createCacheManager(1, 0, 0 ,0));
      cacheManagers.add(createCacheManager(2, 1, 1 ,1));

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_MACHINE);
   }

   public void testExecutionPolicyNotSameSiteFilter() throws ExecutionException, InterruptedException {
      for(int i = 1; i <= 2; i++) {
         cacheManagers.add(createCacheManager(i, 0, 0 ,0));
      }

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_SITE);
   }

   public void testExecutionPolicySameSiteFilter() throws ExecutionException, InterruptedException {
      cacheManagers.add(createCacheManager(1, 0, 0 ,0));
      cacheManagers.add(createCacheManager(2, 1, 0 ,0));

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_SITE);
   }

   public void testExecutionPolicyNotSameRackFilter() throws ExecutionException, InterruptedException {
      for(int i = 1; i <= 2; i++) {
         cacheManagers.add(createCacheManager(i, 0, 0 ,0));
      }

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_RACK);
   }

   public void testExecutionPolicySameRackFilter() throws ExecutionException, InterruptedException {
      cacheManagers.add(createCacheManager(1, 0, 0 ,0));
      cacheManagers.add(createCacheManager(2, 1, 0 ,1));

      waitForClusterToForm();

      executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy.SAME_RACK);
   }

   private void executeDifferentExecutionPolicies(DistributedTaskExecutionPolicy policy) throws ExecutionException, InterruptedException {
      assert address(0) instanceof TopologyAwareAddress;
      assert address(1) instanceof TopologyAwareAddress;

      Cache<Object, Object> cache1 = cache(0, CACHE_NAME);
      Cache<Object, Object> cache2 = cache(1, CACHE_NAME);
      cache1.put("key1", "value1");
      cache1.put("key2", "value2");
      cache1.put("key3", "value3");
      cache1.put("key4", "value4");
      cache2.put("key5", "value5");
      cache2.put("key6", "value6");

      //initiate task from cache1 and select cache2 as target
      DistributedExecutorService des = new DefaultExecutorService(cache1);

      //the same using DistributedTask API
      DistributedTaskBuilder<Boolean> taskBuilder = des.createDistributedTaskBuilder(new SimpleDistributedCallable(true));
      taskBuilder.executionPolicy(policy);

      DistributedTask<Boolean> distributedTask = taskBuilder.build();
      Future<Boolean> future = des.submit(distributedTask, new String[] {"key1", "key6"});

      assert future.get();
   }

   private void updatedSiteInfo(GlobalConfiguration gc1, String s0, String r0, String m0) {
      gc1.setSiteId(s0);
      gc1.setRackId(r0);
      gc1.setMachineId(m0);
   }

   static class SimpleDistributedCallable implements DistributedCallable<String, String, Boolean>,
                                                     Serializable {

      /** The serialVersionUID */
      private static final long serialVersionUID = 623845442163221832L;
      private boolean invokedProperly = false;
      private final boolean hasKeys;

      public SimpleDistributedCallable(boolean hasKeys) {
         this.hasKeys = hasKeys;
      }

      @Override
      public Boolean call() throws Exception {
         return invokedProperly;
      }

      @Override
      public void setEnvironment(Cache<String, String> cache, Set<String> inputKeys) {
         boolean keysProperlySet = hasKeys ? inputKeys != null && !inputKeys.isEmpty()
               : inputKeys != null && inputKeys.isEmpty();
         invokedProperly = cache != null && keysProperlySet;
      }

      public boolean validlyInvoked() {
         return invokedProperly;
      }
   }
}
