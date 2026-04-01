package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests for {@link TransientCacheValue} and {@link TransientMortalCacheValue}.
 */
@Test(groups = "unit", testName = "container.entries.TransientCacheValueTest")
public class TransientCacheValueTest {

   // --- TransientCacheValue tests ---

   public void testTransientCacheValueConstruction() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      assertEquals("val", cv.getValue());
      assertEquals(5000, cv.getMaxIdle());
      assertEquals(1000, cv.getLastUsed());
   }

   public void testTransientCacheValueCanExpire() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      assertTrue(cv.canExpire());
      assertTrue(cv.isMaxIdleExpirable());
   }

   public void testTransientCacheValueIsExpired() {
      long now = System.currentTimeMillis();
      TransientCacheValue cv = new TransientCacheValue("val", 5000, now);
      assertFalse(cv.isExpired(now + 4000));
      assertTrue(cv.isExpired(now + 6000));
   }

   public void testTransientCacheValueNotExpiredWithNegativeMaxIdle() {
      long now = System.currentTimeMillis();
      TransientCacheValue cv = new TransientCacheValue("val", -1, now);
      assertFalse(cv.isExpired(now + 999999));
   }

   public void testTransientCacheValueExpiryTime() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      assertEquals(6000, cv.getExpiryTime());
   }

   public void testTransientCacheValueExpiryTimeNegativeMaxIdle() {
      TransientCacheValue cv = new TransientCacheValue("val", -1, 1000);
      assertEquals(-1, cv.getExpiryTime());
   }

   public void testTransientCacheValueSetMaxIdle() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      cv.setMaxIdle(10000);
      assertEquals(10000, cv.getMaxIdle());
   }

   public void testTransientCacheValueSetLastUsed() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      cv.setLastUsed(2000);
      assertEquals(2000, cv.getLastUsed());
   }

   public void testTransientCacheValueToInternalCacheEntry() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      InternalCacheEntry<?, ?> entry = cv.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof TransientCacheEntry);
      assertEquals("key", entry.getKey());
      assertEquals("val", entry.getValue());
   }

   public void testTransientCacheValueEquals() {
      TransientCacheValue cv1 = new TransientCacheValue("val", 5000, 1000);
      TransientCacheValue cv2 = new TransientCacheValue("val", 5000, 1000);
      TransientCacheValue cv3 = new TransientCacheValue("val", 6000, 1000);
      TransientCacheValue cv4 = new TransientCacheValue("val", 5000, 2000);
      TransientCacheValue cv5 = new TransientCacheValue("other", 5000, 1000);

      assertTrue(cv1.equals(cv2));
      assertFalse(cv1.equals(cv3));
      assertFalse(cv1.equals(cv4));
      assertFalse(cv1.equals(cv5));
      assertTrue(cv1.equals(cv1));
      assertFalse(cv1.equals(null));
      assertFalse(cv1.equals("string"));
   }

   public void testTransientCacheValueHashCode() {
      TransientCacheValue cv1 = new TransientCacheValue("val", 5000, 1000);
      TransientCacheValue cv2 = new TransientCacheValue("val", 5000, 1000);
      assertEquals(cv1.hashCode(), cv2.hashCode());
   }

   public void testTransientCacheValueClone() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      TransientCacheValue cloned = cv.clone();
      assertNotSame(cv, cloned);
      assertEquals(cv, cloned);
      assertEquals(cv.getMaxIdle(), cloned.getMaxIdle());
      assertEquals(cv.getLastUsed(), cloned.getLastUsed());
   }

   public void testTransientCacheValueToString() {
      TransientCacheValue cv = new TransientCacheValue("val", 5000, 1000);
      String str = cv.toString();
      assertNotNull(str);
      assertTrue(str.contains("maxIdle=5000"));
      assertTrue(str.contains("lastUsed=1000"));
   }

   // --- TransientMortalCacheValue tests ---

   public void testTransientMortalCacheValueConstruction() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 500, 10000, 5000, 1000);
      assertEquals("val", cv.getValue());
      assertEquals(500, cv.getCreated());
      assertEquals(10000, cv.getLifespan());
      assertEquals(5000, cv.getMaxIdle());
      assertEquals(1000, cv.getLastUsed());
   }

   public void testTransientMortalCacheValueIsExpiredByLifespan() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 100000, 1000);
      // lifespan expires at created + lifespan = 1000 + 5000 = 6000
      assertFalse(cv.isExpired(5500));
      assertTrue(cv.isExpired(7000));
   }

   public void testTransientMortalCacheValueIsExpiredByMaxIdle() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 100000, 3000, 2000);
      // maxIdle expires at lastUsed + maxIdle = 2000 + 3000 = 5000
      assertFalse(cv.isExpired(4000));
      assertTrue(cv.isExpired(6000));
   }

   public void testTransientMortalCacheValueIsMaxIdleExpirable() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      assertTrue(cv.isMaxIdleExpirable());
   }

   public void testTransientMortalCacheValueSetMaxIdle() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      cv.setMaxIdle(10000);
      assertEquals(10000, cv.getMaxIdle());
   }

   public void testTransientMortalCacheValueSetLastUsed() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      cv.setLastUsed(5000);
      assertEquals(5000, cv.getLastUsed());
   }

   public void testTransientMortalCacheValueToInternalCacheEntry() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      InternalCacheEntry<?, ?> entry = cv.toInternalCacheEntry("key");
      assertNotNull(entry);
      assertTrue(entry instanceof TransientMortalCacheEntry);
      assertEquals("key", entry.getKey());
      assertEquals("val", entry.getValue());
   }

   public void testTransientMortalCacheValueExpiryTimeBothSet() {
      // lifespan expiry: 1000 + 10000 = 11000
      // maxIdle expiry: 2000 + 5000 = 7000
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 10000, 5000, 2000);
      assertEquals(7000, cv.getExpiryTime()); // min(11000, 7000)
   }

   public void testTransientMortalCacheValueExpiryTimeLifespanOnly() {
      // maxIdle = -1, lifespan expiry = 1000 + 5000 = 6000
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, -1, 2000);
      assertEquals(6000, cv.getExpiryTime());
   }

   public void testTransientMortalCacheValueExpiryTimeMaxIdleOnly() {
      // lifespan = -1, maxIdle expiry = 2000 + 3000 = 5000
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, -1, 3000, 2000);
      assertEquals(5000, cv.getExpiryTime());
   }

   public void testTransientMortalCacheValueEquals() {
      TransientMortalCacheValue cv1 = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      TransientMortalCacheValue cv2 = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      TransientMortalCacheValue cv3 = new TransientMortalCacheValue("val", 1000, 5000, 4000, 2000);
      TransientMortalCacheValue cv4 = new TransientMortalCacheValue("val", 1000, 5000, 3000, 3000);

      assertTrue(cv1.equals(cv2));
      assertFalse(cv1.equals(cv3));
      assertFalse(cv1.equals(cv4));
      assertTrue(cv1.equals(cv1));
      assertFalse(cv1.equals(null));
      assertFalse(cv1.equals("string"));
   }

   public void testTransientMortalCacheValueHashCode() {
      TransientMortalCacheValue cv1 = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      TransientMortalCacheValue cv2 = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      assertEquals(cv1.hashCode(), cv2.hashCode());
   }

   public void testTransientMortalCacheValueClone() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      TransientMortalCacheValue cloned = cv.clone();
      assertNotSame(cv, cloned);
      assertEquals(cv, cloned);
   }

   public void testTransientMortalCacheValueToString() {
      TransientMortalCacheValue cv = new TransientMortalCacheValue("val", 1000, 5000, 3000, 2000);
      String str = cv.toString();
      assertNotNull(str);
      assertTrue(str.contains("maxIdle=3000"));
      assertTrue(str.contains("lastUsed=2000"));
   }
}
