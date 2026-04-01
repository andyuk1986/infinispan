package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "container.entries.ExpiryHelperTest")
public class ExpiryHelperTest extends AbstractInfinispanTest {

   // ---- isExpiredMortal ----

   public void testMortalNotExpired() {
      assertFalse(ExpiryHelper.isExpiredMortal(5000, 1000, 5999));
   }

   public void testMortalExpired() {
      assertTrue(ExpiryHelper.isExpiredMortal(5000, 1000, 6001));
   }

   public void testMortalNoLifespan() {
      assertFalse(ExpiryHelper.isExpiredMortal(-1, 1000, Long.MAX_VALUE));
   }

   public void testMortalNoCreated() {
      assertFalse(ExpiryHelper.isExpiredMortal(5000, -1, Long.MAX_VALUE));
   }

   public void testMortalBoundary() {
      assertFalse(ExpiryHelper.isExpiredMortal(5000, 1000, 6000));
   }

   // ---- isExpiredTransient ----

   public void testTransientNotExpired() {
      assertFalse(ExpiryHelper.isExpiredTransient(3000, 1000, 3999));
   }

   public void testTransientExpired() {
      assertTrue(ExpiryHelper.isExpiredTransient(3000, 1000, 4001));
   }

   public void testTransientNoMaxIdle() {
      assertFalse(ExpiryHelper.isExpiredTransient(-1, 1000, Long.MAX_VALUE));
   }

   public void testTransientNoLastUsed() {
      assertFalse(ExpiryHelper.isExpiredTransient(3000, -1, Long.MAX_VALUE));
   }

   // ---- isExpiredTransientMortal ----

   public void testTransientMortalNotExpired() {
      assertFalse(ExpiryHelper.isExpiredTransientMortal(3000, 1000, 5000, 1000, 3999));
   }

   public void testTransientMortalExpiredByMaxIdle() {
      assertTrue(ExpiryHelper.isExpiredTransientMortal(3000, 1000, 5000, 1000, 4001));
   }

   public void testTransientMortalExpiredByLifespan() {
      assertTrue(ExpiryHelper.isExpiredTransientMortal(10000, 1000, 5000, 1000, 6001));
   }

   public void testTransientMortalNeitherSet() {
      assertFalse(ExpiryHelper.isExpiredTransientMortal(-1, -1, -1, -1, Long.MAX_VALUE));
   }

   // ---- mostRecentExpirationTime ----

   public void testMostRecentBothPositive() {
      assertEquals(3000L, ExpiryHelper.mostRecentExpirationTime(3000, 5000));
   }

   public void testMostRecentFirstNegative() {
      assertEquals(5000L, ExpiryHelper.mostRecentExpirationTime(-1, 5000));
   }

   public void testMostRecentSecondNegative() {
      assertEquals(3000L, ExpiryHelper.mostRecentExpirationTime(3000, -1));
   }

   public void testMostRecentBothNegative() {
      assertEquals(-1L, ExpiryHelper.mostRecentExpirationTime(-1, -1));
   }

   public void testMostRecentSecondSmaller() {
      assertEquals(2000L, ExpiryHelper.mostRecentExpirationTime(5000, 2000));
   }
}
