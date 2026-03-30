package org.infinispan.container.entries.metadata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.TimeUnit;

import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "container.entries.metadata.MetadataCacheEntryTest")
public class MetadataCacheEntryTest extends AbstractInfinispanTest {

   private Metadata immortalMetadata() {
      return new EmbeddedMetadata.Builder().build();
   }

   private Metadata mortalMetadata(long lifespan) {
      return new EmbeddedMetadata.Builder().lifespan(lifespan, TimeUnit.MILLISECONDS).build();
   }

   private Metadata transientMetadata(long maxIdle) {
      return new EmbeddedMetadata.Builder().maxIdle(maxIdle, TimeUnit.MILLISECONDS).build();
   }

   private Metadata transientMortalMetadata(long lifespan, long maxIdle) {
      return new EmbeddedMetadata.Builder()
            .lifespan(lifespan, TimeUnit.MILLISECONDS)
            .maxIdle(maxIdle, TimeUnit.MILLISECONDS)
            .build();
   }

   // ---- MetadataImmortalCacheEntry ----

   public void testImmortalCacheEntryBasics() {
      MetadataImmortalCacheEntry entry = new MetadataImmortalCacheEntry("key", "value", immortalMetadata());
      assertEquals("key", entry.getKey());
      assertEquals("value", entry.getValue());
      assertNotNull(entry.getMetadata());
      assertFalse(entry.canExpire());
      assertFalse(entry.isExpired(System.currentTimeMillis()));
   }

   public void testImmortalCacheEntrySetMetadata() {
      MetadataImmortalCacheEntry entry = new MetadataImmortalCacheEntry("key", "value", immortalMetadata());
      Metadata newMeta = mortalMetadata(1000);
      entry.setMetadata(newMeta);
      assertEquals(newMeta, entry.getMetadata());
   }

   public void testImmortalCacheEntryToInternalCacheValue() {
      MetadataImmortalCacheEntry entry = new MetadataImmortalCacheEntry("key", "value", immortalMetadata());
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof MetadataImmortalCacheValue);
   }

   public void testImmortalCacheEntryToString() {
      MetadataImmortalCacheEntry entry = new MetadataImmortalCacheEntry("key", "value", immortalMetadata());
      assertNotNull(entry.toString());
   }

   // ---- MetadataImmortalCacheValue ----

   public void testImmortalCacheValueBasics() {
      MetadataImmortalCacheValue value = new MetadataImmortalCacheValue("val", immortalMetadata());
      assertEquals("val", value.getValue());
      assertNotNull(value.getMetadata());
   }

   public void testImmortalCacheValueToInternalCacheEntry() {
      MetadataImmortalCacheValue value = new MetadataImmortalCacheValue("val", immortalMetadata());
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof MetadataImmortalCacheEntry);
      assertEquals("key", entry.getKey());
   }

   public void testImmortalCacheValueSetMetadata() {
      MetadataImmortalCacheValue value = new MetadataImmortalCacheValue("val", immortalMetadata());
      Metadata newMeta = mortalMetadata(5000);
      value.setMetadata(newMeta);
      assertEquals(newMeta, value.getMetadata());
   }

   // ---- MetadataMortalCacheEntry ----

   public void testMortalCacheEntryBasics() {
      long now = System.currentTimeMillis();
      Metadata meta = mortalMetadata(5000);
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", meta, now);
      assertEquals("key", entry.getKey());
      assertEquals("value", entry.getValue());
      assertEquals(meta, entry.getMetadata());
      assertTrue(entry.canExpire());
      assertEquals(now, entry.getCreated());
      assertEquals(5000, entry.getLifespan());
      assertEquals(-1, entry.getLastUsed());
      assertEquals(-1, entry.getMaxIdle());
   }

   public void testMortalCacheEntryNotExpired() {
      long now = System.currentTimeMillis();
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), now);
      assertFalse(entry.isExpired(now));
      assertFalse(entry.isExpired(now + 4999));
   }

   public void testMortalCacheEntryExpired() {
      long now = System.currentTimeMillis();
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), now);
      assertTrue(entry.isExpired(now + 5001));
   }

   public void testMortalCacheEntryExpiryTime() {
      long now = 1000L;
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), now);
      assertEquals(6000L, entry.getExpiryTime());
   }

   public void testMortalCacheEntryExpiryTimeNoLifespan() {
      long now = 1000L;
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", immortalMetadata(), now);
      assertEquals(-1, entry.getExpiryTime());
   }

   public void testMortalCacheEntryReincarnate() {
      long now = 1000L;
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), now);
      entry.reincarnate(2000L);
      assertEquals(2000L, entry.getCreated());
   }

   public void testMortalCacheEntryToInternalCacheValue() {
      long now = System.currentTimeMillis();
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), now);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof MetadataMortalCacheValue);
   }

   public void testMortalCacheEntryToString() {
      MetadataMortalCacheEntry entry = new MetadataMortalCacheEntry("key", "value", mortalMetadata(5000), 1000L);
      assertNotNull(entry.toString());
   }

   // ---- MetadataMortalCacheValue ----

   public void testMortalCacheValueBasics() {
      Metadata meta = mortalMetadata(5000);
      MetadataMortalCacheValue value = new MetadataMortalCacheValue("val", meta, 1000L);
      assertEquals("val", value.getValue());
      assertEquals(meta, value.getMetadata());
      assertTrue(value.canExpire());
      assertEquals(1000L, value.getCreated());
      assertEquals(5000, value.getLifespan());
   }

   public void testMortalCacheValueExpiryTime() {
      MetadataMortalCacheValue value = new MetadataMortalCacheValue("val", mortalMetadata(5000), 1000L);
      assertEquals(6000L, value.getExpiryTime());
   }

   public void testMortalCacheValueExpiryTimeNoLifespan() {
      MetadataMortalCacheValue value = new MetadataMortalCacheValue("val", immortalMetadata(), 1000L);
      assertEquals(-1, value.getExpiryTime());
   }

   public void testMortalCacheValueExpired() {
      MetadataMortalCacheValue value = new MetadataMortalCacheValue("val", mortalMetadata(5000), 1000L);
      assertFalse(value.isExpired(5999));
      assertTrue(value.isExpired(6001));
   }

   public void testMortalCacheValueToInternalCacheEntry() {
      MetadataMortalCacheValue value = new MetadataMortalCacheValue("val", mortalMetadata(5000), 1000L);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof MetadataMortalCacheEntry);
   }

   // ---- MetadataTransientCacheEntry ----

   public void testTransientCacheEntryBasics() {
      long now = System.currentTimeMillis();
      Metadata meta = transientMetadata(3000);
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", meta, now);
      assertEquals("key", entry.getKey());
      assertTrue(entry.canExpire());
      assertTrue(entry.canExpireMaxIdle());
      assertEquals(now, entry.getLastUsed());
      assertEquals(-1, entry.getCreated());
      assertEquals(-1, entry.getLifespan());
      assertEquals(3000, entry.getMaxIdle());
   }

   public void testTransientCacheEntryExpiry() {
      long now = 1000L;
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", transientMetadata(3000), now);
      assertFalse(entry.isExpired(3999));
      assertTrue(entry.isExpired(4001));
   }

   public void testTransientCacheEntryExpiryTime() {
      long now = 1000L;
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", transientMetadata(3000), now);
      assertEquals(4000L, entry.getExpiryTime());
   }

   public void testTransientCacheEntryExpiryTimeNoMaxIdle() {
      long now = 1000L;
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", immortalMetadata(), now);
      assertEquals(-1, entry.getExpiryTime());
   }

   public void testTransientCacheEntryTouch() {
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", transientMetadata(3000), 1000L);
      entry.touch(2000L);
      assertEquals(2000L, entry.getLastUsed());
   }

   public void testTransientCacheEntryToInternalCacheValue() {
      MetadataTransientCacheEntry entry = new MetadataTransientCacheEntry("key", "value", transientMetadata(3000), 1000L);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof MetadataTransientCacheValue);
   }

   // ---- MetadataTransientCacheValue ----

   public void testTransientCacheValueBasics() {
      Metadata meta = transientMetadata(3000);
      MetadataTransientCacheValue value = new MetadataTransientCacheValue("val", meta, 1000L);
      assertEquals("val", value.getValue());
      assertTrue(value.canExpire());
      assertTrue(value.isMaxIdleExpirable());
      assertEquals(1000L, value.getLastUsed());
      assertEquals(3000, value.getMaxIdle());
   }

   public void testTransientCacheValueExpiryTime() {
      MetadataTransientCacheValue value = new MetadataTransientCacheValue("val", transientMetadata(3000), 1000L);
      assertEquals(4000L, value.getExpiryTime());
   }

   public void testTransientCacheValueExpiryTimeNoMaxIdle() {
      MetadataTransientCacheValue value = new MetadataTransientCacheValue("val", immortalMetadata(), 1000L);
      assertEquals(-1, value.getExpiryTime());
   }

   public void testTransientCacheValueExpired() {
      MetadataTransientCacheValue value = new MetadataTransientCacheValue("val", transientMetadata(3000), 1000L);
      assertFalse(value.isExpired(3999));
      assertTrue(value.isExpired(4001));
   }

   public void testTransientCacheValueToInternalCacheEntry() {
      MetadataTransientCacheValue value = new MetadataTransientCacheValue("val", transientMetadata(3000), 1000L);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof MetadataTransientCacheEntry);
   }

   // ---- MetadataTransientMortalCacheEntry ----

   public void testTransientMortalCacheEntryBasics() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      assertEquals("key", entry.getKey());
      assertTrue(entry.canExpire());
      assertTrue(entry.canExpireMaxIdle());
      assertEquals(1000L, entry.getCreated());
      assertEquals(1000L, entry.getLastUsed());
      assertEquals(5000, entry.getLifespan());
      assertEquals(3000, entry.getMaxIdle());
   }

   public void testTransientMortalCacheEntryConstructorNow() {
      long now = System.currentTimeMillis();
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, now);
      assertEquals(now, entry.getCreated());
      assertEquals(now, entry.getLastUsed());
   }

   public void testTransientMortalCacheEntryExpiryTimeBothSet() {
      // lifespan expiry = 1000 + 5000 = 6000, maxIdle expiry = 1000 + 3000 = 4000, min = 4000
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      assertEquals(4000L, entry.getExpiryTime());
   }

   public void testTransientMortalCacheEntryExpiryTimeOnlyLifespan() {
      Metadata meta = mortalMetadata(5000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      // maxIdle = -1, so expiry is lifespan only: 1000 + 5000 = 6000
      assertEquals(6000L, entry.getExpiryTime());
   }

   public void testTransientMortalCacheEntryExpiryTimeOnlyMaxIdle() {
      Metadata meta = transientMetadata(3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      // lifespan = -1, so expiry is maxIdle only: 1000 + 3000 = 4000
      assertEquals(4000L, entry.getExpiryTime());
   }

   public void testTransientMortalCacheEntryExpiry() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      assertFalse(entry.isExpired(3999));
      assertTrue(entry.isExpired(4001));
   }

   public void testTransientMortalCacheEntryTouch() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      entry.touch(2000L);
      assertEquals(2000L, entry.getLastUsed());
   }

   public void testTransientMortalCacheEntryReincarnate() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      entry.reincarnate(2000L);
      assertEquals(2000L, entry.getCreated());
   }

   public void testTransientMortalCacheEntryToInternalCacheValue() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheEntry entry = new MetadataTransientMortalCacheEntry("key", "value", meta, 1000L, 1000L);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof MetadataTransientMortalCacheValue);
   }

   // ---- MetadataTransientMortalCacheValue ----

   public void testTransientMortalCacheValueBasics() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertEquals("val", value.getValue());
      assertTrue(value.isMaxIdleExpirable());
      assertEquals(1000L, value.getLastUsed());
      assertEquals(3000, value.getMaxIdle());
   }

   public void testTransientMortalCacheValueExpiryTimeBothSet() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertEquals(4000L, value.getExpiryTime());
   }

   public void testTransientMortalCacheValueExpiryTimeOnlyLifespan() {
      Metadata meta = mortalMetadata(5000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertEquals(6000L, value.getExpiryTime());
   }

   public void testTransientMortalCacheValueExpiryTimeOnlyMaxIdle() {
      Metadata meta = transientMetadata(3000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertEquals(4000L, value.getExpiryTime());
   }

   public void testTransientMortalCacheValueExpired() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertFalse(value.isExpired(3999));
      assertTrue(value.isExpired(4001));
   }

   public void testTransientMortalCacheValueToInternalCacheEntry() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue value = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof MetadataTransientMortalCacheEntry);
   }

   public void testTransientMortalCacheValueEquals() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue v1 = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      MetadataTransientMortalCacheValue v2 = new MetadataTransientMortalCacheValue("val", meta, 1000L, 1000L);
      assertTrue(v1.equals(v2));
      assertEquals(v1.hashCode(), v2.hashCode());
   }

   public void testTransientMortalCacheValueNotEquals() {
      Metadata meta = transientMortalMetadata(5000, 3000);
      MetadataTransientMortalCacheValue v1 = new MetadataTransientMortalCacheValue("val1", meta, 1000L, 1000L);
      MetadataTransientMortalCacheValue v2 = new MetadataTransientMortalCacheValue("val2", meta, 1000L, 1000L);
      assertFalse(v1.equals(v2));
   }

   // ---- L1MetadataInternalCacheEntry ----

   public void testL1MetadataInternalCacheEntry() {
      L1MetadataInternalCacheEntry entry = new L1MetadataInternalCacheEntry("key", "value", mortalMetadata(5000), 1000L);
      assertTrue(entry.isL1Entry());
      assertTrue(entry.canExpire());
   }
}
