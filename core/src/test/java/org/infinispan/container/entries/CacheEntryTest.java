package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "container.entries.CacheEntryTest")
public class CacheEntryTest extends AbstractInfinispanTest {

   // ---- ImmortalCacheEntry ----

   public void testImmortalCacheEntry() {
      ImmortalCacheEntry entry = new ImmortalCacheEntry("key", "value");
      assertEquals("key", entry.getKey());
      assertEquals("value", entry.getValue());
      assertFalse(entry.canExpire());
      assertFalse(entry.isExpired(Long.MAX_VALUE));
      assertEquals(-1, entry.getExpiryTime());
      assertNotNull(entry.getMetadata());
   }

   public void testImmortalCacheEntryToInternalCacheValue() {
      ImmortalCacheEntry entry = new ImmortalCacheEntry("key", "value");
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof ImmortalCacheValue);
   }

   public void testImmortalCacheEntryEquals() {
      ImmortalCacheEntry e1 = new ImmortalCacheEntry("key", "value");
      ImmortalCacheEntry e2 = new ImmortalCacheEntry("key", "value");
      assertTrue(e1.equals(e2));
      assertEquals(e1.hashCode(), e2.hashCode());
   }

   public void testImmortalCacheEntryNotEquals() {
      ImmortalCacheEntry e1 = new ImmortalCacheEntry("key1", "value");
      ImmortalCacheEntry e2 = new ImmortalCacheEntry("key2", "value");
      assertFalse(e1.equals(e2));
   }

   public void testImmortalCacheEntryToString() {
      ImmortalCacheEntry entry = new ImmortalCacheEntry("key", "value");
      assertNotNull(entry.toString());
   }

   // ---- ImmortalCacheValue ----

   public void testImmortalCacheValue() {
      ImmortalCacheValue value = new ImmortalCacheValue("val");
      assertEquals("val", value.getValue());
      assertFalse(value.canExpire());
      assertFalse(value.isExpired(Long.MAX_VALUE));
      assertEquals(-1, value.getExpiryTime());
   }

   public void testImmortalCacheValueToInternalCacheEntry() {
      ImmortalCacheValue value = new ImmortalCacheValue("val");
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof ImmortalCacheEntry);
      assertEquals("key", entry.getKey());
   }

   public void testImmortalCacheValueEquals() {
      ImmortalCacheValue v1 = new ImmortalCacheValue("val");
      ImmortalCacheValue v2 = new ImmortalCacheValue("val");
      assertTrue(v1.equals(v2));
      assertEquals(v1.hashCode(), v2.hashCode());
   }

   // ---- MortalCacheEntry ----

   public void testMortalCacheEntry() {
      MortalCacheEntry entry = new MortalCacheEntry("key", "value", 5000, 1000);
      assertEquals("key", entry.getKey());
      assertEquals("value", entry.getValue());
      assertTrue(entry.canExpire());
      assertEquals(5000, entry.getLifespan());
      assertEquals(1000, entry.getCreated());
      assertEquals(-1, entry.getLastUsed());
      assertEquals(-1, entry.getMaxIdle());
   }

   public void testMortalCacheEntryExpiry() {
      MortalCacheEntry entry = new MortalCacheEntry("key", "value", 5000, 1000);
      assertFalse(entry.isExpired(5999));
      assertTrue(entry.isExpired(6001));
      assertEquals(6000L, entry.getExpiryTime());
   }

   public void testMortalCacheEntryToInternalCacheValue() {
      MortalCacheEntry entry = new MortalCacheEntry("key", "value", 5000, 1000);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof MortalCacheValue);
   }

   public void testMortalCacheEntryReincarnate() {
      MortalCacheEntry entry = new MortalCacheEntry("key", "value", 5000, 1000);
      entry.reincarnate(2000);
      assertEquals(2000, entry.getCreated());
   }

   // ---- MortalCacheValue ----

   public void testMortalCacheValue() {
      // constructor: MortalCacheValue(value, created, lifespan)
      MortalCacheValue value = new MortalCacheValue("val", 1000, 5000);
      assertEquals("val", value.getValue());
      assertTrue(value.canExpire());
      assertEquals(5000, value.getLifespan());
      assertEquals(1000, value.getCreated());
   }

   public void testMortalCacheValueExpiry() {
      MortalCacheValue value = new MortalCacheValue("val", 1000, 5000);
      assertFalse(value.isExpired(5999));
      assertTrue(value.isExpired(6001));
      assertEquals(6000L, value.getExpiryTime());
   }

   public void testMortalCacheValueToInternalCacheEntry() {
      MortalCacheValue value = new MortalCacheValue("val", 1000, 5000);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof MortalCacheEntry);
   }

   // ---- TransientCacheEntry ----

   public void testTransientCacheEntry() {
      TransientCacheEntry entry = new TransientCacheEntry("key", "value", 3000, 1000);
      assertEquals("key", entry.getKey());
      assertTrue(entry.canExpire());
      assertEquals(3000, entry.getMaxIdle());
      assertEquals(1000, entry.getLastUsed());
      assertEquals(-1, entry.getCreated());
      assertEquals(-1, entry.getLifespan());
   }

   public void testTransientCacheEntryExpiry() {
      TransientCacheEntry entry = new TransientCacheEntry("key", "value", 3000, 1000);
      assertFalse(entry.isExpired(3999));
      assertTrue(entry.isExpired(4001));
      assertEquals(4000L, entry.getExpiryTime());
   }

   public void testTransientCacheEntryTouch() {
      TransientCacheEntry entry = new TransientCacheEntry("key", "value", 3000, 1000);
      entry.touch(2000);
      assertEquals(2000, entry.getLastUsed());
   }

   public void testTransientCacheEntryToInternalCacheValue() {
      TransientCacheEntry entry = new TransientCacheEntry("key", "value", 3000, 1000);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof TransientCacheValue);
   }

   // ---- TransientCacheValue ----

   public void testTransientCacheValue() {
      // constructor: TransientCacheValue(value, maxIdle, lastUsed)
      TransientCacheValue value = new TransientCacheValue("val", 3000, 1000);
      assertEquals("val", value.getValue());
      assertTrue(value.canExpire());
      assertEquals(3000, value.getMaxIdle());
      assertEquals(1000, value.getLastUsed());
   }

   public void testTransientCacheValueToInternalCacheEntry() {
      TransientCacheValue value = new TransientCacheValue("val", 3000, 1000);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof TransientCacheEntry);
   }

   // ---- TransientMortalCacheEntry ----

   public void testTransientMortalCacheEntry() {
      TransientMortalCacheEntry entry = new TransientMortalCacheEntry("key", "value", 3000, 5000, 1000, 1000);
      assertEquals("key", entry.getKey());
      assertTrue(entry.canExpire());
      assertEquals(3000, entry.getMaxIdle());
      assertEquals(5000, entry.getLifespan());
      assertEquals(1000, entry.getCreated());
      assertEquals(1000, entry.getLastUsed());
   }

   public void testTransientMortalCacheEntryExpiry() {
      TransientMortalCacheEntry entry = new TransientMortalCacheEntry("key", "value", 3000, 5000, 1000, 1000);
      assertFalse(entry.isExpired(3999));
      assertTrue(entry.isExpired(4001));
   }

   public void testTransientMortalCacheEntryToInternalCacheValue() {
      TransientMortalCacheEntry entry = new TransientMortalCacheEntry("key", "value", 3000, 5000, 1000, 1000);
      InternalCacheValue<?> value = entry.toInternalCacheValue();
      assertNotNull(value);
      assertTrue(value instanceof TransientMortalCacheValue);
   }

   // ---- TransientMortalCacheValue ----

   public void testTransientMortalCacheValue() {
      TransientMortalCacheValue value = new TransientMortalCacheValue("val", 3000, 5000, 1000, 1000);
      assertEquals("val", value.getValue());
      assertTrue(value.canExpire());
   }

   public void testTransientMortalCacheValueToInternalCacheEntry() {
      TransientMortalCacheValue value = new TransientMortalCacheValue("val", 3000, 5000, 1000, 1000);
      var entry = value.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof TransientMortalCacheEntry);
   }

   // ---- NullCacheEntry ----

   public void testNullCacheEntry() {
      NullCacheEntry entry = NullCacheEntry.getInstance();
      assertNotNull(entry);
      assertNull(entry.getKey());
      assertNull(entry.getValue());
      assertNull(entry.getMetadata());
      assertFalse(entry.isChanged());
      assertFalse(entry.isCreated());
      assertFalse(entry.isRemoved());
      assertEquals(-1, entry.getLifespan());
      assertEquals(-1, entry.getMaxIdle());
   }

   // ---- L1InternalCacheEntry ----

   public void testL1InternalCacheEntry() {
      L1InternalCacheEntry entry = new L1InternalCacheEntry("key", "value", 5000, 1000);
      assertTrue(entry.isL1Entry());
      assertEquals("key", entry.getKey());
      assertTrue(entry.canExpire());
   }
}
