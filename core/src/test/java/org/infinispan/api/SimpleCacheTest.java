package org.infinispan.api;

import static org.infinispan.functional.FunctionalTestUtils.await;
import static org.infinispan.test.TestingUtil.extractInterceptorChain;
import static org.infinispan.test.TestingUtil.k;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.cache.impl.AbstractDelegatingCache;
import org.infinispan.cache.impl.SimpleCacheImpl;
import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.commons.CacheException;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.versioning.NumericVersion;
import org.infinispan.context.Flag;
import org.infinispan.interceptors.AsyncInterceptorChain;
import org.infinispan.interceptors.impl.InvocationContextInterceptor;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.infinispan.partitionhandling.AvailabilityMode;
import org.infinispan.stats.Stats;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.TransactionMode;
import org.testng.annotations.Test;

/**
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
@Test(groups = "functional", testName = "api.SimpleCacheTest")
public class SimpleCacheTest extends APINonTxTest {

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.simpleCache(true);
      EmbeddedCacheManager cm = TestCacheManagerFactory.createCacheManager(cb);

      cache = AbstractDelegatingCache.unwrapCache(cm.getCache());
      assertTrue(cache instanceof SimpleCacheImpl);
      return cm;
   }

   private AdvancedCache<Object, Object> advancedCache() {
      return cache.getAdvancedCache();
   }

   public void testFindInterceptor() {
      AsyncInterceptorChain interceptorChain = extractInterceptorChain(cache());
      assertNotNull(interceptorChain);
      assertNull(interceptorChain.findInterceptorExtending(InvocationContextInterceptor.class));
   }

   @Test(expectedExceptions = CacheConfigurationException.class)
   public void testTransactions() {
      new ConfigurationBuilder().simpleCache(true)
            .transaction().transactionMode(TransactionMode.TRANSACTIONAL).build();
   }

   @Test(expectedExceptions = CacheConfigurationException.class)
   public void testBatching() {
      new ConfigurationBuilder().simpleCache(true).invocationBatching().enable(true).build();
   }

   @Test(expectedExceptions = CacheConfigurationException.class, expectedExceptionsMessageRegExp = "ISPN000381: This configuration is not supported for simple cache")
   public void testIndexing() {
      new ConfigurationBuilder().simpleCache(true).indexing().enable().build();
   }

   @Test(dataProvider = "lockedStreamActuallyLocks", expectedExceptions = UnsupportedOperationException.class)
   @Override
   public void testLockedStreamActuallyLocks(BiConsumer<Cache<Object, Object>, CacheEntry<Object, Object>> consumer,
                                             boolean forEachOrInvokeAll) throws Throwable {
      super.testLockedStreamActuallyLocks(consumer, forEachOrInvokeAll);
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   @Override
   public void testLockedStreamSetValue() {
      super.testLockedStreamSetValue();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   @Override
   public void testLockedStreamWithinLockedStream() {
      super.testLockedStreamWithinLockedStream();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   @Override
   public void testLockedStreamInvokeAllFilteredSet() {
      super.testLockedStreamInvokeAllFilteredSet();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   @Override
   public void testLockedStreamInvokeAllPut() {
      super.testLockedStreamInvokeAllPut();
   }

   public void testStatistics() {
      Configuration cfg = new ConfigurationBuilder().simpleCache(true).statistics().enabled(true).build();
      String name = "statsCache";
      cacheManager.defineConfiguration(name, cfg);
      Cache<Object, Object> cache = cacheManager.getCache(name);
      assertEquals(0L, cache.getAdvancedCache().getStats().getStores());
      cache.put("key", "value");
      assertEquals(1L, cache.getAdvancedCache().getStats().getStores());
   }

   public void testEvictionWithStatistics() {
      int KEY_COUNT = 5;
      Configuration cfg = new ConfigurationBuilder()
            .simpleCache(true)
            .memory().maxCount(1)
            .statistics().enable()
            .build();
      String name = "evictionCache";
      cacheManager.defineConfiguration(name, cfg);
      Cache<Object, Object> cache = cacheManager.getCache(name);
      for (int i = 0; i < KEY_COUNT; i++) {
         cache.put("key" + i, "value");
      }

      Stats stats = cache.getAdvancedCache().getStats();
      assertEquals(1, stats.getCurrentNumberOfEntriesInMemory());
      assertEquals(KEY_COUNT, stats.getStores());
      assertEquals(KEY_COUNT - 1, stats.getEvictions());
   }

   public void testPutAsyncEntry() {
      AdvancedCache<Object, Object> c = cache.getAdvancedCache();
      Metadata metadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(1))
            .lifespan(25_000)
            .maxIdle(30_000)
            .build();
      assertNull(await(c.putAsync("k", "v1", metadata)));
      assertEquals("v1", cache.get("k"));

      Metadata updatedMetadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(2))
            .lifespan(35_000)
            .maxIdle(42_000)
            .build();
      CacheEntry<Object, Object> previousEntry = await(c.putAsyncEntry("k", "v2", updatedMetadata));
      assertEquals("k", previousEntry.getKey());
      assertEquals("v1", previousEntry.getValue());
      assertNotNull(previousEntry.getMetadata());
      assertMetadata(metadata, previousEntry.getMetadata());

      CacheEntry<Object, Object> currentEntry = c.getCacheEntry("k");
      assertEquals("k", currentEntry.getKey());
      assertEquals("v2", currentEntry.getValue());
      assertNotNull(currentEntry.getMetadata());
      assertMetadata(updatedMetadata, currentEntry.getMetadata());
   }

   public void testPutIfAbsentAsyncEntry() {
      AdvancedCache<Object, Object> c = cache.getAdvancedCache();
      Metadata metadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(1))
            .lifespan(25_000)
            .maxIdle(30_000)
            .build();
      assertNull(await(c.putAsync("k", "v1", metadata)));
      assertEquals("v1", c.get("k"));

      Metadata updatedMetadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(2))
            .lifespan(35_000)
            .maxIdle(42_000)
            .build();
      CacheEntry<Object, Object> previousEntry = await(c.putIfAbsentAsyncEntry("k", "v2", updatedMetadata));
      assertEquals("k", previousEntry.getKey());
      assertEquals("v1", previousEntry.getValue());

      assertMetadata(metadata, previousEntry.getMetadata());

      CacheEntry<Object, Object> currentEntry = await(c.getCacheEntryAsync("k"));
      assertEquals("k", currentEntry.getKey());
      assertEquals("v1", currentEntry.getValue());
      assertNotNull(currentEntry.getMetadata());
      assertMetadata(metadata, currentEntry.getMetadata());
   }

   public void testRemoveAsyncEntry() {
      AdvancedCache<Object, Object> c = cache.getAdvancedCache();
      Metadata metadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(1))
            .lifespan(25_000)
            .maxIdle(30_000)
            .build();
      assertNull(await(c.putAsync("k", "v", metadata)));

      CacheEntry<Object, Object> currentEntry = await(c.getCacheEntryAsync("k"));
      assertEquals("k", currentEntry.getKey());
      assertEquals("v", currentEntry.getValue());
      assertNotNull(currentEntry.getMetadata());
      assertMetadata(metadata, currentEntry.getMetadata());

      CacheEntry<Object, Object> previousEntry = await(c.removeAsyncEntry("k"));
      assertEquals("k", previousEntry.getKey());
      assertEquals("v", previousEntry.getValue());

      assertMetadata(metadata, previousEntry.getMetadata());
      assertNull(c.get("k"));

      assertNull(await(c.removeAsyncEntry("k")));
   }

   public void testReplaceAsyncEntryNonExistingKey() {
      Metadata metadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(1))
            .lifespan(25_000)
            .maxIdle(30_000)
            .build();
      CompletableFuture<CacheEntry<Object, Object>> f = cache.getAdvancedCache().replaceAsyncEntry("k", "v", metadata);
      assertNull(await(f));
   }

   public void testComputeWithExistingValue() {
      assertNull(cache.put("k", "v"));
      assertEquals("v", cache.compute("k", (key, value) -> value));
   }

   public void testReplaceAsyncEntryExistingKey() {
      AdvancedCache<Object, Object> c = cache.getAdvancedCache();
      Metadata metadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(1))
            .lifespan(25_000)
            .maxIdle(30_000)
            .build();
      assertNull(await(c.putAsync("k", "v1", metadata)));

      Metadata updatedMetadata = new EmbeddedMetadata.Builder()
            .version(new NumericVersion(2))
            .lifespan(35_000)
            .maxIdle(42_000)
            .build();
      CacheEntry<Object, Object> previousEntry = await(c.replaceAsyncEntry("k", "v2", updatedMetadata));
      assertEquals("k", previousEntry.getKey());
      assertEquals("v1", previousEntry.getValue());
      assertMetadata(metadata, previousEntry.getMetadata());

      CacheEntry<Object, Object> currentEntry = await(c.getCacheEntryAsync("k"));
      assertEquals("k", currentEntry.getKey());
      assertEquals("v2", currentEntry.getValue());
      assertNotNull(currentEntry.getMetadata());
      assertMetadata(updatedMetadata, currentEntry.getMetadata());
   }

   private void assertMetadata(Metadata expected, Metadata actual) {
      assertEquals(expected.version(), actual.version());
      assertEquals(expected.lifespan(), actual.lifespan());
      assertEquals(expected.maxIdle(), actual.maxIdle());
   }

   public void testGetGroup() {
      assertEquals(0, cache.getAdvancedCache().getGroup("group").size());
   }

   public void testGetAvailability() {
      assertEquals(AvailabilityMode.AVAILABLE, cache.getAdvancedCache().getAvailability());
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testSetAvailability() {
      cache.getAdvancedCache().setAvailability(AvailabilityMode.DEGRADED_MODE);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "ISPN000696:.*")
   public void testQuery() {
      cache.query("some query");
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "ISPN000696:.*")
   public void testContinuousQuery() {
      cache.continuousQuery();
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "ISPN000378:.*")
   public void testStartBatch() {
      cache.startBatch();
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "ISPN000378:.*")
   public void testEndBatch() {
      cache.endBatch(true);
   }

   @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = "ISPN000377:.*")
   public void testLock() {
      cache.getAdvancedCache().lock(k());
   }

   @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = "ISPN000377:.*")
   public void testLockCollection() {
      cache.getAdvancedCache().lock(List.of(k()));
   }

   // --- Coverage tests for SimpleCacheImpl uncovered methods ---

   public void testPutForExternalReadWithLifespan() {
      advancedCache().putForExternalRead("perK1", "v1", 10000, TimeUnit.MILLISECONDS);
      assertEquals("v1", cache.get("perK1"));
   }

   public void testPutForExternalReadWithLifespanAndMaxIdle() {
      advancedCache().putForExternalRead("perK2", "v2", 10000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS);
      assertEquals("v2", cache.get("perK2"));
   }

   public void testPutForExternalReadWithMetadata() {
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).maxIdle(5000).build();
      advancedCache().putForExternalRead("perK3", "v3", metadata);
      assertEquals("v3", cache.get("perK3"));
   }

   public void testPutForExternalReadDoesNotOverwrite() {
      cache.put("perK4", "original");
      advancedCache().putForExternalRead("perK4", "new");
      assertEquals("original", cache.get("perK4"));
   }

   public void testContainsValue() {
      cache.put("cvK", "cvV");
      assertTrue(cache.containsValue("cvV"));
      assertFalse(cache.containsValue("nonExistent"));
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testContainsValueNull() {
      cache.containsValue(null);
   }

   public void testGetAll() {
      cache.put("ga1", "v1");
      cache.put("ga2", "v2");
      Set<Object> keys = new HashSet<>(Arrays.asList("ga1", "ga2", "ga3"));
      Map<Object, Object> result = advancedCache().getAll(keys);
      assertEquals(2, result.size());
      assertEquals("v1", result.get("ga1"));
      assertEquals("v2", result.get("ga2"));
   }

   public void testGetAllAsync() {
      cache.put("gaa1", "v1");
      Map<Object, Object> result = advancedCache().getAllAsync(Collections.singleton("gaa1")).join();
      assertEquals(1, result.size());
      assertEquals("v1", result.get("gaa1"));
   }

   public void testRemoveGroup() {
      advancedCache().removeGroup("anyGroup");
   }

   public void testToString() {
      String s = cache.toString();
      assertNotNull(s);
      assertTrue(s.contains("Cache"));
   }

   public void testGetCacheName() {
      SimpleCacheImpl<?, ?> simpleCache = (SimpleCacheImpl<?, ?>) cache;
      String cacheName = simpleCache.getCacheName();
      assertNotNull(cacheName);
      assertTrue(cacheName.contains("local"));
   }

   public void testGetVersion() {
      SimpleCacheImpl<?, ?> simpleCache = (SimpleCacheImpl<?, ?>) cache;
      String version = simpleCache.getVersion();
      assertNotNull(version);
   }

   public void testGetConfigurationAsProperties() {
      SimpleCacheImpl<?, ?> simpleCache = (SimpleCacheImpl<?, ?>) cache;
      Properties props = simpleCache.getConfigurationAsProperties();
      assertNotNull(props);
   }

   public void testGetCacheStatus() {
      SimpleCacheImpl<?, ?> simpleCache = (SimpleCacheImpl<?, ?>) cache;
      String status = simpleCache.getCacheStatus();
      assertNotNull(status);
      assertEquals("RUNNING", status);
   }

   public void testWithFlags() {
      AdvancedCache<Object, Object> flagged = advancedCache().withFlags(Flag.CACHE_MODE_LOCAL);
      assertNotNull(flagged);
      assertTrue(flagged == advancedCache());
   }

   public void testWithFlagsCollection() {
      AdvancedCache<Object, Object> flagged = advancedCache().withFlags(Collections.singleton(Flag.CACHE_MODE_LOCAL));
      assertNotNull(flagged);
      assertTrue(flagged == advancedCache());
   }

   public void testNoFlags() {
      AdvancedCache<Object, Object> result = advancedCache().noFlags();
      assertNotNull(result);
      assertTrue(result == advancedCache());
   }

   public void testTransform() {
      AdvancedCache<Object, Object> result = advancedCache().transform(c -> c);
      assertTrue(result == advancedCache());
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testLockAs() {
      advancedCache().lockAs(new Object());
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testWithStorageMediaType() {
      advancedCache().withStorageMediaType();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testGetKeyDataConversion() {
      advancedCache().getKeyDataConversion();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testGetValueDataConversion() {
      advancedCache().getValueDataConversion();
   }

   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testAddStorageFormatFilteredListenerAsync() throws Exception {
      advancedCache().addStorageFormatFilteredListenerAsync(new Object(), null, null, null)
            .toCompletableFuture().get();
   }

   public void testGetBatchContainer() {
      assertNull(advancedCache().getBatchContainer());
   }

   public void testGetLockManager() {
      assertNull(advancedCache().getLockManager());
   }

   public void testGetStats() {
      assertNotNull(advancedCache().getStats());
   }

   public void testGetXAResource() {
      assertNull(advancedCache().getXAResource());
   }

   public void testGetClassLoader() {
      assertNull(advancedCache().getClassLoader());
   }

   public void testGetTransactionManager() {
      assertNull(advancedCache().getTransactionManager());
   }

   public void testGetRpcManager() {
      assertNull(advancedCache().getRpcManager());
   }

   public void testPutAsyncWithLifespanAndMaxIdle() {
      CompletableFuture<Object> f = cache.putAsync("pak1", "v1", 10000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS);
      assertNull(f.join());
      assertEquals("v1", cache.get("pak1"));
   }

   public void testPutAllAsync() {
      Map<Object, Object> map = Map.of("paa1", "v1", "paa2", "v2");
      cache.putAllAsync(map).join();
      assertEquals("v1", cache.get("paa1"));
      assertEquals("v2", cache.get("paa2"));
   }

   public void testPutAllAsyncLifespan() {
      Map<Object, Object> map = Map.of("paal1", "v1");
      cache.putAllAsync(map, 10000, TimeUnit.MILLISECONDS).join();
      assertEquals("v1", cache.get("paal1"));
   }

   public void testPutAllAsyncLifespanMaxIdle() {
      Map<Object, Object> map = Map.of("paalm1", "v1");
      cache.putAllAsync(map, 10000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS).join();
      assertEquals("v1", cache.get("paalm1"));
   }

   public void testPutAllAsyncMetadata() {
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      Map<Object, Object> map = Map.of("paam1", "v1");
      advancedCache().putAllAsync(map, metadata).toCompletableFuture().join();
      assertEquals("v1", cache.get("paam1"));
   }

   public void testPutIfAbsentAsync() {
      CompletableFuture<Object> f = cache.putIfAbsentAsync("piaK", "v1");
      assertNull(f.join());
      assertEquals("v1", cache.get("piaK"));
      assertEquals("v1", cache.putIfAbsentAsync("piaK", "v2").join());
   }

   public void testPutIfAbsentAsyncLifespanMaxIdle() {
      CompletableFuture<Object> f = cache.putIfAbsentAsync("pialm", "v1", 10000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS);
      assertNull(f.join());
      assertEquals("v1", cache.get("pialm"));
   }

   public void testPutIfAbsentAsyncMetadata() {
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      CompletableFuture<Object> f = advancedCache().putIfAbsentAsync("piamK", "v1", metadata);
      assertNull(f.join());
      assertEquals("v1", cache.get("piamK"));
   }

   public void testReplaceAsyncMetadata() {
      cache.put("ramK", "v1");
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      assertEquals("v1", advancedCache().replaceAsync("ramK", "v2", metadata).join());
      assertEquals("v2", cache.get("ramK"));
   }

   public void testReplaceAsyncOldValueMetadata() {
      cache.put("raomK", "v1");
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      assertTrue(advancedCache().replaceAsync("raomK", "v1", "v2", metadata).join());
      assertEquals("v2", cache.get("raomK"));
   }

   public void testPutIfAbsentMetadata() {
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      assertNull(advancedCache().putIfAbsent("piamK2", "v1", metadata));
      assertEquals("v1", cache.get("piamK2"));
      assertEquals("v1", advancedCache().putIfAbsent("piamK2", "v2", metadata));
   }

   public void testReplaceOldValueMetadata() {
      cache.put("rovmK", "v1");
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(10000).build();
      assertFalse(advancedCache().replace("rovmK", "wrong", "v2", metadata));
      assertEquals("v1", cache.get("rovmK"));
      assertTrue(advancedCache().replace("rovmK", "v1", "v2", metadata));
      assertEquals("v2", cache.get("rovmK"));
   }

   public void testClear() {
      cache.put("c1", "v1");
      cache.put("c2", "v2");
      assertFalse(cache.isEmpty());
      cache.clear();
      assertTrue(cache.isEmpty());
      assertEquals(0, cache.size());
   }

   public void testClearAsync() {
      cache.put("ca1", "v1");
      cache.clearAsync().join();
      assertTrue(cache.isEmpty());
   }

   public void testSizeAndIsEmpty() {
      assertTrue(cache.isEmpty());
      assertEquals(0, cache.size());
      cache.put("s1", "v1");
      cache.put("s2", "v2");
      assertFalse(cache.isEmpty());
      assertEquals(2, cache.size());
   }

   public void testSizeAsync() {
      cache.put("sa1", "v1");
      cache.put("sa2", "v2");
      long size = advancedCache().sizeAsync().join();
      assertEquals(2L, size);
   }

   public void testRemoveKeyValue() {
      cache.put("rkvK", "v1");
      assertFalse(cache.remove("rkvK", "wrong"));
      assertEquals("v1", cache.get("rkvK"));
      assertTrue(cache.remove("rkvK", "v1"));
      assertNull(cache.get("rkvK"));
   }

   public void testRemoveAsync() {
      cache.put("raK", "v1");
      assertEquals("v1", cache.removeAsync("raK").join());
      assertNull(cache.get("raK"));
   }

   public void testRemoveAsyncKeyValue() {
      cache.put("rakvK", "v1");
      assertFalse(cache.removeAsync("rakvK", "wrong").join());
      assertTrue(cache.removeAsync("rakvK", "v1").join());
   }

   public void testGetAsync() {
      cache.put("gaK", "v1");
      assertEquals("v1", cache.getAsync("gaK").join());
      assertNull(cache.getAsync("nonExistent").join());
   }

   public void testMergeNewKey() {
      Object result = cache.merge("mergeNew", "initial", (oldVal, newVal) -> oldVal + "_" + newVal);
      assertEquals("initial", result);
      assertEquals("initial", cache.get("mergeNew"));
   }

   public void testMergeExistingKey() {
      cache.put("mergeExist", "old");
      Object result = cache.merge("mergeExist", "new", (oldVal, newVal) -> oldVal + "_" + newVal);
      assertEquals("old_new", result);
   }

   public void testMergeRemoval() {
      cache.put("mergeRm", "old");
      Object result = cache.merge("mergeRm", "new", (oldVal, newVal) -> null);
      assertNull(result);
      assertNull(cache.get("mergeRm"));
   }

   public void testComputeNewKey() {
      Object result = cache.compute("compNew", (k, v) -> "created");
      assertEquals("created", result);
      assertEquals("created", cache.get("compNew"));
   }

   public void testComputeExistingKey() {
      cache.put("compExist", "old");
      Object result = cache.compute("compExist", (k, v) -> v + "_updated");
      assertEquals("old_updated", result);
   }

   public void testComputeRemoval() {
      cache.put("compRm", "old");
      Object result = cache.compute("compRm", (k, v) -> null);
      assertNull(result);
      assertNull(cache.get("compRm"));
   }

   public void testComputeIfAbsentNewKey() {
      Object result = cache.computeIfAbsent("ciaNew", k -> "created");
      assertEquals("created", result);
   }

   public void testComputeIfAbsentExistingKey() {
      cache.put("ciaExist", "old");
      Object result = cache.computeIfAbsent("ciaExist", k -> "new");
      assertEquals("old", result);
   }

   public void testComputeIfAbsentReturnsNull() {
      Object result = cache.computeIfAbsent("ciaNull", k -> null);
      assertNull(result);
   }

   public void testComputeIfPresentExistingKey() {
      cache.put("cipExist", "old");
      Object result = cache.computeIfPresent("cipExist", (k, v) -> v + "_updated");
      assertEquals("old_updated", result);
   }

   public void testComputeIfPresentAbsentKey() {
      Object result = cache.computeIfPresent("cipAbsent", (k, v) -> "new");
      assertNull(result);
   }

   public void testComputeIfPresentRemoval() {
      cache.put("cipRm", "old");
      Object result = cache.computeIfPresent("cipRm", (k, v) -> null);
      assertNull(result);
      assertNull(cache.get("cipRm"));
   }

   public void testKeySetOperations() {
      cache.put("ks1", "v1");
      cache.put("ks2", "v2");
      cache.put("ks3", "v3");

      Set<Object> keySet = cache.keySet();
      assertEquals(3, keySet.size());
      assertTrue(keySet.contains("ks1"));

      keySet.retainAll(Arrays.asList("ks1", "ks2"));
      assertEquals(2, cache.size());

      keySet.removeAll(Collections.singleton("ks1"));
      assertEquals(1, cache.size());
      assertNull(cache.get("ks1"));

      keySet.clear();
      assertTrue(cache.isEmpty());
   }

   public void testValuesOperations() {
      cache.put("vs1", "v1");
      cache.put("vs2", "v2");
      cache.put("vs3", "v3");

      assertTrue(cache.values().remove("v1"));
      assertFalse(cache.values().remove("nonExistent"));
      assertEquals(2, cache.size());

      cache.values().removeAll(Arrays.asList("v2", "v3"));
      assertTrue(cache.isEmpty());
   }

   public void testValuesRetainAll() {
      cache.put("vr1", "v1");
      cache.put("vr2", "v2");
      cache.put("vr3", "v3");
      cache.values().retainAll(Collections.singleton("v2"));
      assertEquals(1, cache.size());
      assertEquals("v2", cache.get("vr2"));
   }

   public void testEntrySetOperations() {
      cache.put("es1", "v1");
      cache.put("es2", "v2");

      Set<Map.Entry<Object, Object>> entrySet = cache.entrySet();
      assertFalse(entrySet.isEmpty());
      assertEquals(2, entrySet.size());

      Object[] arr = entrySet.toArray();
      assertEquals(2, arr.length);
   }

   public void testEntrySetRemove() {
      cache.put("esr1", "v1");
      Set<Map.Entry<Object, Object>> entrySet = cache.entrySet();
      Map.Entry<Object, Object> entry = entrySet.iterator().next();
      assertTrue(entrySet.remove(entry));
      assertTrue(cache.isEmpty());
   }

   public void testEntrySetRemoveNonEntry() {
      cache.put("esrn1", "v1");
      Set<Map.Entry<Object, Object>> entrySet = cache.entrySet();
      assertFalse(entrySet.remove("notAnEntry"));
   }

   public void testEntrySetRetainAll() {
      cache.put("esra1", "v1");
      cache.put("esra2", "v2");
      Set<Map.Entry<Object, Object>> entrySet = cache.entrySet();
      Map.Entry<Object, Object> first = entrySet.iterator().next();
      entrySet.retainAll(Collections.singleton(first));
      assertEquals(1, cache.size());
   }

   public void testEntrySetRemoveAll() {
      cache.put("esrma1", "v1");
      cache.put("esrma2", "v2");
      Set<Map.Entry<Object, Object>> entrySet = cache.entrySet();
      Map.Entry<Object, Object> first = entrySet.iterator().next();
      entrySet.removeAll(Collections.singleton(first));
      assertEquals(1, cache.size());
   }

   public void testCacheEntrySet() {
      cache.put("ces1", "v1");
      Set<CacheEntry<Object, Object>> entrySet = advancedCache().cacheEntrySet();
      assertEquals(1, entrySet.size());
      CacheEntry<Object, Object> entry = entrySet.iterator().next();
      assertEquals("ces1", entry.getKey());
      assertEquals("v1", entry.getValue());
   }

   public void testWithSubject() {
      AdvancedCache<Object, Object> result = advancedCache().withSubject(null);
      assertTrue(result == advancedCache());
   }

   public void testGetCacheEntry() {
      cache.put("gceK", "v1");
      CacheEntry<Object, Object> entry = advancedCache().getCacheEntry("gceK");
      assertNotNull(entry);
      assertEquals("gceK", entry.getKey());
      assertEquals("v1", entry.getValue());
   }

   public void testGetCacheEntryAsync() {
      cache.put("gceaK", "v1");
      CacheEntry<Object, Object> entry = advancedCache().getCacheEntryAsync("gceaK").join();
      assertNotNull(entry);
      assertEquals("v1", entry.getValue());
   }

   public void testGetCacheEntryNull() {
      CacheEntry<Object, Object> entry = advancedCache().getCacheEntry("nonExistent");
      assertNull(entry);
   }

   public void testComputeAsyncNewKey() {
      Object result = advancedCache().computeAsync("casK", (k, v) -> "created").join();
      assertEquals("created", result);
   }

   public void testComputeIfAbsentAsyncNewKey() {
      Object result = advancedCache().computeIfAbsentAsync("ciaaK", k -> "created").join();
      assertEquals("created", result);
   }

   public void testComputeIfPresentAsyncExisting() {
      cache.put("cipaK", "old");
      Object result = advancedCache().computeIfPresentAsync("cipaK", (k, v) -> "updated").join();
      assertEquals("updated", result);
   }

   public void testTouch() {
      cache.put("touchK", "v1");
      Boolean result = advancedCache().touch("touchK", false).toCompletableFuture().join();
      assertTrue(result);
   }

   public void testTouchNonExistent() {
      Boolean result = advancedCache().touch("nonExistent", false).toCompletableFuture().join();
      assertFalse(result);
   }

   public void testGetAdvancedCache() {
      assertTrue(cache.getAdvancedCache() == cache);
   }

   public void testGetComponentRegistry() {
      assertNotNull(((SimpleCacheImpl<?, ?>) cache).getComponentRegistry());
   }

   public void testGetCacheConfiguration() {
      assertNotNull(advancedCache().getCacheConfiguration());
      assertTrue(advancedCache().getCacheConfiguration().simpleCache());
   }

   public void testGetCacheManager() {
      assertNotNull(cache.getCacheManager());
      assertTrue(cache.getCacheManager() == cacheManager);
   }

   public void testGetStatus() {
      assertEquals(org.infinispan.lifecycle.ComponentStatus.RUNNING, cache.getStatus());
   }

   public void testGetName() {
      assertNotNull(cache.getName());
   }

   public void testGetExpirationManager() {
      assertNotNull(advancedCache().getExpirationManager());
   }

   public void testGetDistributionManager() {
      advancedCache().getDistributionManager();
   }

   public void testGetAuthorizationManager() {
      advancedCache().getAuthorizationManager();
   }
}
