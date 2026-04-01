package org.infinispan.reactive.internal.util;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "reactive.internal.util.BackpressureHelperTest")
public class BackpressureHelperTest extends AbstractInfinispanTest {

   // ---- addCap ----

   public void testAddCapNormal() {
      assertEquals(5L, BackpressureHelper.addCap(2L, 3L));
   }

   public void testAddCapOverflow() {
      assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(Long.MAX_VALUE, 1L));
   }

   public void testAddCapBothLarge() {
      assertEquals(Long.MAX_VALUE, BackpressureHelper.addCap(Long.MAX_VALUE - 1, 2));
   }

   public void testAddCapNoOverflow() {
      assertEquals(10L, BackpressureHelper.addCap(4L, 6L));
   }

   // ---- multiplyCap ----

   public void testMultiplyCapNormal() {
      assertEquals(6L, BackpressureHelper.multiplyCap(2L, 3L));
   }

   public void testMultiplyCapOverflow() {
      assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(Long.MAX_VALUE, 2L));
   }

   public void testMultiplyCapSmallValues() {
      // Values where (a | b) >>> 31 == 0, so no overflow check
      assertEquals(100L, BackpressureHelper.multiplyCap(10L, 10L));
   }

   public void testMultiplyCapLargeValues() {
      // Values where (a | b) >>> 31 != 0, triggers overflow check
      long a = 1L << 32;
      long b = 1L << 32;
      assertEquals(Long.MAX_VALUE, BackpressureHelper.multiplyCap(a, b));
   }

   public void testMultiplyCapOneLargeOneSmall() {
      long a = 1L << 32;
      long b = 2L;
      assertEquals(a * b, BackpressureHelper.multiplyCap(a, b));
   }

   // ---- add ----

   public void testAddNormal() {
      AtomicLong requested = new AtomicLong(5);
      long prev = BackpressureHelper.add(requested, 3);
      assertEquals(5L, prev);
      assertEquals(8L, requested.get());
   }

   public void testAddAlreadyMaxValue() {
      AtomicLong requested = new AtomicLong(Long.MAX_VALUE);
      long prev = BackpressureHelper.add(requested, 10);
      assertEquals(Long.MAX_VALUE, prev);
      assertEquals(Long.MAX_VALUE, requested.get());
   }

   public void testAddResultCapped() {
      AtomicLong requested = new AtomicLong(Long.MAX_VALUE - 1);
      long prev = BackpressureHelper.add(requested, 5);
      assertEquals(Long.MAX_VALUE - 1, prev);
      assertEquals(Long.MAX_VALUE, requested.get());
   }

   // ---- addCancel ----

   public void testAddCancelNormal() {
      AtomicLong requested = new AtomicLong(5);
      long prev = BackpressureHelper.addCancel(requested, 3);
      assertEquals(5L, prev);
      assertEquals(8L, requested.get());
   }

   public void testAddCancelMinValue() {
      AtomicLong requested = new AtomicLong(Long.MIN_VALUE);
      long prev = BackpressureHelper.addCancel(requested, 10);
      assertEquals(Long.MIN_VALUE, prev);
      assertEquals(Long.MIN_VALUE, requested.get());
   }

   public void testAddCancelMaxValue() {
      AtomicLong requested = new AtomicLong(Long.MAX_VALUE);
      long prev = BackpressureHelper.addCancel(requested, 10);
      assertEquals(Long.MAX_VALUE, prev);
      assertEquals(Long.MAX_VALUE, requested.get());
   }

   // ---- produced ----

   public void testProducedNormal() {
      AtomicLong requested = new AtomicLong(10);
      long result = BackpressureHelper.produced(requested, 3);
      assertEquals(7L, result);
      assertEquals(7L, requested.get());
   }

   public void testProducedMaxValue() {
      AtomicLong requested = new AtomicLong(Long.MAX_VALUE);
      long result = BackpressureHelper.produced(requested, 5);
      assertEquals(Long.MAX_VALUE, result);
      assertEquals(Long.MAX_VALUE, requested.get());
   }

   // ---- producedCancel ----

   public void testProducedCancelNormal() {
      AtomicLong requested = new AtomicLong(10);
      long result = BackpressureHelper.producedCancel(requested, 3);
      assertEquals(7L, result);
      assertEquals(7L, requested.get());
   }

   public void testProducedCancelMinValue() {
      AtomicLong requested = new AtomicLong(Long.MIN_VALUE);
      long result = BackpressureHelper.producedCancel(requested, 5);
      assertEquals(Long.MIN_VALUE, result);
      assertEquals(Long.MIN_VALUE, requested.get());
   }

   public void testProducedCancelMaxValue() {
      AtomicLong requested = new AtomicLong(Long.MAX_VALUE);
      long result = BackpressureHelper.producedCancel(requested, 5);
      assertEquals(Long.MAX_VALUE, result);
      assertEquals(Long.MAX_VALUE, requested.get());
   }
}
