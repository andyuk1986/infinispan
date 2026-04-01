package org.infinispan.api.flags;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.infinispan.AdvancedCache;
import org.infinispan.cache.impl.CacheImpl;
import org.infinispan.cache.impl.DecoratedCache;
import org.infinispan.commons.util.EnumUtil;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

/**
 * @author Sanne Grinovero &lt;sanne@infinispan.org&gt; (C) 2011 Red Hat Inc.
 */
@Test(groups = "functional", testName = "api.flags.DecoratedCacheTest")
public class DecoratedCacheTest  extends SingleCacheManagerTest {

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      ConfigurationBuilder c = getDefaultStandaloneCacheConfig(false);
      EmbeddedCacheManager cm = TestCacheManagerFactory.createCacheManager(c);
      cache = cm.getCache();
      return cm;
   }

   private AdvancedCache<Object, Object> flagged() {
      return cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
   }

   // --- Flag management tests ---

   public void testWithFlagsCollection() {
      Collection<Flag> flagList = Arrays.asList(Flag.FAIL_SILENTLY, Flag.FORCE_ASYNCHRONOUS);
      AdvancedCache<Object, Object> decorated = cache.getAdvancedCache().withFlags(flagList);
      assertTrue(decorated.containsFlag(Flag.FAIL_SILENTLY));
      assertTrue(decorated.containsFlag(Flag.FORCE_ASYNCHRONOUS));
   }

   public void testNoFlags() {
      AdvancedCache<Object, Object> decorated = cache.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY);
      AdvancedCache<Object, Object> noFlagsCache = decorated.noFlags();
      assertFalse(noFlagsCache.containsFlag(Flag.FAIL_SILENTLY));
   }

   public void testLockAs() {
      AdvancedCache<Object, Object> decorated = cache.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY);
      Object lockOwner = new Object();
      AdvancedCache<Object, Object> lockedCache = decorated.lockAs(lockOwner);
      assertTrue(lockedCache.containsFlag(Flag.FAIL_SILENTLY));
   }

   public void testLockAsSameOwnerReturnsSameInstance() {
      Object lockOwner = new Object();
      AdvancedCache<Object, Object> lockedCache = cache.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY).lockAs(lockOwner);
      AdvancedCache<Object, Object> lockedAgain = lockedCache.lockAs(lockOwner);
      assertTrue(lockedCache == lockedAgain);
   }

   public void testLockedStreamWithoutLockOwner() {
      AdvancedCache<Object, Object> adv = cache.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY);
      assertNotNull(adv.lockedStream());
   }

   public void testGetClassLoader() {
      AdvancedCache<Object, Object> adv = cache.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY);
      assertNotNull(adv.getClassLoader());
   }

   // --- put with lifespan ---

   public void testPutWithLifespan() {
      flagged().put("k1", "v1", 100000, TimeUnit.MILLISECONDS);
      assertEquals("v1", cache.get("k1"));
   }

   public void testPutWithLifespanAndMaxIdle() {
      flagged().put("k2", "v2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("v2", cache.get("k2"));
   }

   // --- putIfAbsent with lifespan ---

   public void testPutIfAbsentWithLifespan() {
      flagged().putIfAbsent("ka1", "va1", 100000, TimeUnit.MILLISECONDS);
      assertEquals("va1", cache.get("ka1"));
   }

   public void testPutIfAbsentWithLifespanAndMaxIdle() {
      flagged().putIfAbsent("ka2", "va2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("va2", cache.get("ka2"));
   }

   // --- putAll with lifespan ---

   public void testPutAllWithLifespan() {
      Map<String, String> data = new HashMap<>();
      data.put("pa1", "pv1");
      data.put("pa2", "pv2");
      flagged().putAll(data, 100000, TimeUnit.MILLISECONDS);
      assertEquals("pv1", cache.get("pa1"));
      assertEquals("pv2", cache.get("pa2"));
   }

   public void testPutAllWithLifespanAndMaxIdle() {
      Map<String, String> data = new HashMap<>();
      data.put("pb1", "pv1");
      data.put("pb2", "pv2");
      flagged().putAll(data, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("pv1", cache.get("pb1"));
      assertEquals("pv2", cache.get("pb2"));
   }

   // --- replace with lifespan ---

   public void testReplaceWithLifespan() {
      cache.put("rk1", "rv1");
      flagged().replace("rk1", "rv2", 100000, TimeUnit.MILLISECONDS);
      assertEquals("rv2", cache.get("rk1"));
   }

   public void testReplaceConditionalWithLifespan() {
      cache.put("rk2", "rv1");
      boolean replaced = flagged().replace("rk2", "rv1", "rv2", 100000, TimeUnit.MILLISECONDS);
      assertTrue(replaced);
      assertEquals("rv2", cache.get("rk2"));
   }

   public void testReplaceWithLifespanAndMaxIdle() {
      cache.put("rk3", "rv1");
      flagged().replace("rk3", "rv2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("rv2", cache.get("rk3"));
   }

   public void testReplaceConditionalWithLifespanAndMaxIdle() {
      cache.put("rk4", "rv1");
      boolean replaced = flagged().replace("rk4", "rv1", "rv2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertTrue(replaced);
      assertEquals("rv2", cache.get("rk4"));
   }

   // --- putForExternalRead with lifespan ---

   public void testPutForExternalReadWithLifespan() {
      flagged().putForExternalRead("pfr1", "pfrv1", 100000, TimeUnit.MILLISECONDS);
      assertEquals("pfrv1", cache.get("pfr1"));
   }

   public void testPutForExternalReadWithLifespanAndMaxIdle() {
      flagged().putForExternalRead("pfr2", "pfrv2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("pfrv2", cache.get("pfr2"));
   }

   // --- containsValue ---

   public void testContainsValue() {
      cache.put("cv1", "value1");
      assertTrue(flagged().containsValue("value1"));
      assertFalse(flagged().containsValue("nonexistent"));
   }

   // --- getAll and getAllCacheEntries ---

   public void testGetAll() {
      cache.put("ga1", "gv1");
      cache.put("ga2", "gv2");
      Set<Object> keys = new HashSet<>(Arrays.asList("ga1", "ga2", "ga3"));
      Map<Object, Object> result = flagged().getAll(keys);
      assertEquals("gv1", result.get("ga1"));
      assertEquals("gv2", result.get("ga2"));
      assertNull(result.get("ga3"));
   }

   public void testGetAllCacheEntries() {
      cache.put("gae1", "gev1");
      cache.put("gae2", "gev2");
      Set<Object> keys = new HashSet<>(Arrays.asList("gae1", "gae2"));
      Map<Object, CacheEntry<Object, Object>> result = flagged().getAllCacheEntries(keys);
      assertEquals("gev1", result.get("gae1").getValue());
      assertEquals("gev2", result.get("gae2").getValue());
   }

   // --- putAsync with lifespan ---

   public void testPutAsyncWithLifespanAndMaxIdle() throws Exception {
      CompletableFuture<Object> f = flagged().putAsync("pak1", "pav1", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertNotNull(f);
      f.get();
      assertEquals("pav1", cache.get("pak1"));
   }

   // --- putAllAsync ---

   public void testPutAllAsyncNoArgs() throws Exception {
      Map<String, String> data = Collections.singletonMap("paa1", "paav1");
      CompletableFuture<Void> f = flagged().putAllAsync(data);
      f.get();
      assertEquals("paav1", cache.get("paa1"));
   }

   public void testPutAllAsyncWithLifespan() throws Exception {
      Map<String, String> data = Collections.singletonMap("paa2", "paav2");
      CompletableFuture<Void> f = flagged().putAllAsync(data, 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("paav2", cache.get("paa2"));
   }

   public void testPutAllAsyncWithLifespanAndMaxIdle() throws Exception {
      Map<String, String> data = Collections.singletonMap("paa3", "paav3");
      CompletableFuture<Void> f = flagged().putAllAsync(data, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("paav3", cache.get("paa3"));
   }

   // --- putIfAbsentAsync ---

   public void testPutIfAbsentAsyncNoArgs() throws Exception {
      CompletableFuture<Object> f = flagged().putIfAbsentAsync("pia1", "piav1");
      f.get();
      assertEquals("piav1", cache.get("pia1"));
   }

   public void testPutIfAbsentAsyncWithLifespan() throws Exception {
      CompletableFuture<Object> f = flagged().putIfAbsentAsync("pia2", "piav2", 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("piav2", cache.get("pia2"));
   }

   public void testPutIfAbsentAsyncWithLifespanAndMaxIdle() throws Exception {
      CompletableFuture<Object> f = flagged().putIfAbsentAsync("pia3", "piav3", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("piav3", cache.get("pia3"));
   }

   // --- replaceAsync ---

   public void testReplaceAsyncNoArgs() throws Exception {
      cache.put("ra1", "rav1");
      CompletableFuture<Object> f = flagged().replaceAsync("ra1", "rav2");
      f.get();
      assertEquals("rav2", cache.get("ra1"));
   }

   public void testReplaceAsyncWithLifespan() throws Exception {
      cache.put("ra2", "rav1");
      CompletableFuture<Object> f = flagged().replaceAsync("ra2", "rav2", 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("rav2", cache.get("ra2"));
   }

   public void testReplaceAsyncWithLifespanAndMaxIdle() throws Exception {
      cache.put("ra3", "rav1");
      CompletableFuture<Object> f = flagged().replaceAsync("ra3", "rav2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("rav2", cache.get("ra3"));
   }

   public void testReplaceConditionalAsyncNoArgs() throws Exception {
      cache.put("rca1", "rcav1");
      CompletableFuture<Boolean> f = flagged().replaceAsync("rca1", "rcav1", "rcav2");
      assertTrue(f.get());
      assertEquals("rcav2", cache.get("rca1"));
   }

   public void testReplaceConditionalAsyncWithLifespan() throws Exception {
      cache.put("rca2", "rcav1");
      CompletableFuture<Boolean> f = flagged().replaceAsync("rca2", "rcav1", "rcav2", 100000, TimeUnit.MILLISECONDS);
      assertTrue(f.get());
      assertEquals("rcav2", cache.get("rca2"));
   }

   public void testReplaceConditionalAsyncWithLifespanAndMaxIdle() throws Exception {
      cache.put("rca3", "rcav1");
      CompletableFuture<Boolean> f = flagged().replaceAsync("rca3", "rcav1", "rcav2", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertTrue(f.get());
      assertEquals("rcav2", cache.get("rca3"));
   }

   // --- compute with lifespan ---

   public void testComputeWithLifespan() {
      cache.put("ck1", "cv1");
      flagged().compute("ck1", (k, v) -> v + "_updated", 100000, TimeUnit.MILLISECONDS);
      assertEquals("cv1_updated", cache.get("ck1"));
   }

   public void testComputeWithLifespanAndMaxIdle() {
      cache.put("ck2", "cv1");
      flagged().compute("ck2", (k, v) -> v + "_updated", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("cv1_updated", cache.get("ck2"));
   }

   // --- computeIfPresent with lifespan ---

   public void testComputeIfPresentWithLifespan() {
      cache.put("cip1", "cipv1");
      flagged().computeIfPresent("cip1", (k, v) -> v + "_updated", 100000, TimeUnit.MILLISECONDS);
      assertEquals("cipv1_updated", cache.get("cip1"));
   }

   public void testComputeIfPresentWithLifespanAndMaxIdle() {
      cache.put("cip2", "cipv1");
      flagged().computeIfPresent("cip2", (k, v) -> v + "_updated", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("cipv1_updated", cache.get("cip2"));
   }

   // --- computeIfAbsent with lifespan ---

   public void testComputeIfAbsentWithLifespan() {
      flagged().computeIfAbsent("cia1", k -> "computed_" + k, 100000, TimeUnit.MILLISECONDS);
      assertEquals("computed_cia1", cache.get("cia1"));
   }

   public void testComputeIfAbsentWithLifespanAndMaxIdle() {
      flagged().computeIfAbsent("cia2", k -> "computed_" + k, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("computed_cia2", cache.get("cia2"));
   }

   // --- merge with lifespan ---

   public void testMergeWithLifespan() {
      cache.put("mk1", "mv1");
      flagged().merge("mk1", "mv2", (v1, v2) -> v1 + "_" + v2, 100000, TimeUnit.MILLISECONDS);
      assertEquals("mv1_mv2", cache.get("mk1"));
   }

   public void testMergeWithLifespanAndMaxIdle() {
      cache.put("mk2", "mv1");
      flagged().merge("mk2", "mv2", (v1, v2) -> v1 + "_" + v2, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      assertEquals("mv1_mv2", cache.get("mk2"));
   }

   // --- computeAsync with lifespan ---

   public void testComputeAsyncNoArgs() throws Exception {
      cache.put("cak1", "cav1");
      CompletableFuture<Object> f = flagged().computeAsync("cak1", (k, v) -> v + "_async");
      f.get();
      assertEquals("cav1_async", cache.get("cak1"));
   }

   public void testComputeAsyncWithLifespan() throws Exception {
      cache.put("cak2", "cav1");
      CompletableFuture<Object> f = flagged().computeAsync("cak2", (k, v) -> v + "_async", 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("cav1_async", cache.get("cak2"));
   }

   public void testComputeAsyncWithLifespanAndMaxIdle() throws Exception {
      cache.put("cak3", "cav1");
      CompletableFuture<Object> f = flagged().computeAsync("cak3", (k, v) -> v + "_async", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("cav1_async", cache.get("cak3"));
   }

   // --- computeIfPresentAsync with lifespan ---

   public void testComputeIfPresentAsyncWithLifespan() throws Exception {
      cache.put("cipak1", "cipav1");
      CompletableFuture<Object> f = flagged().computeIfPresentAsync("cipak1", (k, v) -> v + "_async", 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("cipav1_async", cache.get("cipak1"));
   }

   public void testComputeIfPresentAsyncWithLifespanAndMaxIdle() throws Exception {
      cache.put("cipak2", "cipav1");
      CompletableFuture<Object> f = flagged().computeIfPresentAsync("cipak2", (k, v) -> v + "_async", 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("cipav1_async", cache.get("cipak2"));
   }

   // --- computeIfAbsentAsync with lifespan ---

   public void testComputeIfAbsentAsyncNoArgs() throws Exception {
      CompletableFuture<Object> f = flagged().computeIfAbsentAsync("ciaak1", k -> "computed_" + k);
      f.get();
      assertEquals("computed_ciaak1", cache.get("ciaak1"));
   }

   public void testComputeIfAbsentAsyncWithLifespan() throws Exception {
      CompletableFuture<Object> f = flagged().computeIfAbsentAsync("ciaak2", k -> "computed_" + k, 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("computed_ciaak2", cache.get("ciaak2"));
   }

   public void testComputeIfAbsentAsyncWithLifespanAndMaxIdle() throws Exception {
      CompletableFuture<Object> f = flagged().computeIfAbsentAsync("ciaak3", k -> "computed_" + k, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("computed_ciaak3", cache.get("ciaak3"));
   }

   // --- mergeAsync with lifespan ---

   public void testMergeAsyncNoArgs() throws Exception {
      cache.put("mak1", "mav1");
      CompletableFuture<Object> f = flagged().mergeAsync("mak1", "mav2", (v1, v2) -> v1 + "_" + v2);
      f.get();
      assertEquals("mav1_mav2", cache.get("mak1"));
   }

   public void testMergeAsyncWithLifespan() throws Exception {
      cache.put("mak2", "mav1");
      CompletableFuture<Object> f = flagged().mergeAsync("mak2", "mav2", (v1, v2) -> v1 + "_" + v2, 100000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("mav1_mav2", cache.get("mak2"));
   }

   public void testMergeAsyncWithLifespanAndMaxIdle() throws Exception {
      cache.put("mak3", "mav1");
      CompletableFuture<Object> f = flagged().mergeAsync("mak3", "mav2", (v1, v2) -> v1 + "_" + v2, 100000, TimeUnit.MILLISECONDS, 50000, TimeUnit.MILLISECONDS);
      f.get();
      assertEquals("mav1_mav2", cache.get("mak3"));
   }

   // --- addListenerAsync ---

   public void testAddListenerAsync() throws Exception {
      TestCacheListener listener = new TestCacheListener();
      flagged().addListenerAsync(listener).toCompletableFuture().get();
      flagged().removeListenerAsync(listener).toCompletableFuture().get();
   }

   // --- cachePublisher ---

   public void testCachePublisher() {
      assertNotNull(flagged().cachePublisher());
   }

   @org.infinispan.notifications.Listener
   public static class TestCacheListener {
   }

   public void testDecoratedCacheFlagsSet() {
      CacheImpl impl = new CacheImpl("baseCache");
      DecoratedCache decoratedCache = new DecoratedCache(impl, EnumUtil.EMPTY_BIT_SET);
      DecoratedCache nofailCache = (DecoratedCache) decoratedCache.withFlags(Flag.FAIL_SILENTLY);
      EnumSet<Flag> nofailCacheFlags = EnumUtil.enumSetOf(nofailCache.getFlagsBitSet(), Flag.class);
      assert nofailCacheFlags.contains(Flag.FAIL_SILENTLY);
      assert nofailCacheFlags.size() == 1;
      DecoratedCache asyncNoFailCache = (DecoratedCache) nofailCache.withFlags(Flag.FORCE_ASYNCHRONOUS);
      EnumSet<Flag> asyncNofailCacheFlags = EnumUtil.enumSetOf(asyncNoFailCache.getFlagsBitSet(), Flag.class);
      assert asyncNofailCacheFlags.size() == 2;
      assert asyncNofailCacheFlags.contains(Flag.FAIL_SILENTLY);
      assert asyncNofailCacheFlags.contains(Flag.FORCE_ASYNCHRONOUS);
      AdvancedCache again = asyncNoFailCache.withFlags(Flag.FAIL_SILENTLY);
      assert again == asyncNoFailCache; // as FAIL_SILENTLY was already specified
   }

}
