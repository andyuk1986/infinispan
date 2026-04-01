package org.infinispan.stream.impl.intops.primitive.l;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.testng.annotations.Test;

/**
 * Tests for long stream intermediate operations.
 */
@Test(groups = "unit", testName = "stream.impl.intops.primitive.l.LongStreamOpsTest")
public class LongStreamOpsTest {

   // --- MapLongOperation ---

   public void testMapLongPerform() {
      MapLongOperation op = new MapLongOperation(x -> x * 3);
      long[] result = op.perform(LongStream.of(1, 2, 3)).toArray();
      assertArrayEquals(new long[]{3, 6, 9}, result);
   }

   public void testMapLongMapFlowable() {
      MapLongOperation op = new MapLongOperation(x -> x + 100);
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L))).toList().blockingGet();
      assertEquals(Arrays.asList(101L, 102L), result);
   }

   // --- MapToObjLongOperation ---

   public void testMapToObjLongPerform() {
      MapToObjLongOperation<String> op = new MapToObjLongOperation<>(String::valueOf);
      List<String> result = op.perform(LongStream.of(10, 20)).collect(Collectors.toList());
      assertEquals(Arrays.asList("10", "20"), result);
   }

   public void testMapToObjLongMapFlowable() {
      MapToObjLongOperation<String> op = new MapToObjLongOperation<>(l -> "L" + l);
      List<String> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L))).toList().blockingGet();
      assertEquals(Arrays.asList("L1", "L2"), result);
   }

   // --- MapToDoubleLongOperation ---

   public void testMapToDoubleLongPerform() {
      MapToDoubleLongOperation op = new MapToDoubleLongOperation(l -> l * 0.5);
      double[] result = op.perform(LongStream.of(4, 6)).toArray();
      assertEquals(2.0, result[0], 0.001);
      assertEquals(3.0, result[1], 0.001);
   }

   public void testMapToDoubleLongMapFlowable() {
      MapToDoubleLongOperation op = new MapToDoubleLongOperation(l -> l + 0.1);
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L))).toList().blockingGet();
      assertEquals(1.1, result.get(0), 0.001);
      assertEquals(2.1, result.get(1), 0.001);
   }

   // --- MapToIntLongOperation ---

   public void testMapToIntLongPerform() {
      MapToIntLongOperation op = new MapToIntLongOperation(l -> (int) l);
      int[] result = op.perform(LongStream.of(100, 200)).toArray();
      assertArrayEquals(new int[]{100, 200}, result);
   }

   public void testMapToIntLongMapFlowable() {
      MapToIntLongOperation op = new MapToIntLongOperation(l -> (int) (l % 10));
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(15L, 27L))).toList().blockingGet();
      assertEquals(Arrays.asList(5, 7), result);
   }

   // --- DistinctLongOperation ---

   public void testDistinctLongPerform() {
      DistinctLongOperation op = DistinctLongOperation.getInstance();
      long[] result = op.perform(LongStream.of(1, 2, 1, 3, 2)).toArray();
      assertArrayEquals(new long[]{1, 2, 3}, result);
   }

   public void testDistinctLongSingleton() {
      assertTrue(DistinctLongOperation.getInstance() == DistinctLongOperation.getInstance());
   }

   public void testDistinctLongMapFlowable() {
      DistinctLongOperation op = DistinctLongOperation.getInstance();
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L, 1L))).toList().blockingGet();
      assertEquals(Arrays.asList(1L, 2L), result);
   }

   // --- AsDoubleLongOperation ---

   public void testAsDoubleLongPerform() {
      AsDoubleLongOperation op = AsDoubleLongOperation.getInstance();
      double[] result = op.perform(LongStream.of(1, 2)).toArray();
      assertEquals(1.0, result[0], 0.001);
      assertEquals(2.0, result[1], 0.001);
   }

   public void testAsDoubleLongMapFlowable() {
      AsDoubleLongOperation op = AsDoubleLongOperation.getInstance();
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(5L, 10L))).toList().blockingGet();
      assertEquals(Arrays.asList(5.0, 10.0), result);
   }

   public void testAsDoubleLongSingleton() {
      assertTrue(AsDoubleLongOperation.getInstance() == AsDoubleLongOperation.getInstance());
   }

   // --- BoxedLongOperation ---

   public void testBoxedLongPerform() {
      BoxedLongOperation op = BoxedLongOperation.getInstance();
      List<Long> result = op.perform(LongStream.of(1, 2, 3)).collect(Collectors.toList());
      assertEquals(Arrays.asList(1L, 2L, 3L), result);
   }

   public void testBoxedLongMapFlowable() {
      BoxedLongOperation op = BoxedLongOperation.getInstance();
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L))).toList().blockingGet();
      assertEquals(Arrays.asList(1L, 2L), result);
   }

   public void testBoxedLongSingleton() {
      assertTrue(BoxedLongOperation.getInstance() == BoxedLongOperation.getInstance());
   }

   // --- LimitLongOperation ---

   public void testLimitLongPerform() {
      LimitLongOperation op = new LimitLongOperation(2);
      long[] result = op.perform(LongStream.of(10, 20, 30, 40)).toArray();
      assertArrayEquals(new long[]{10, 20}, result);
   }

   public void testLimitLongGetLimit() {
      LimitLongOperation op = new LimitLongOperation(5);
      assertEquals(5L, op.getLimit());
   }

   public void testLimitLongMapFlowable() {
      LimitLongOperation op = new LimitLongOperation(3);
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1L, 2L, 3L, 4L, 5L))).toList().blockingGet();
      assertEquals(Arrays.asList(1L, 2L, 3L), result);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitLongZero() {
      new LimitLongOperation(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitLongNegative() {
      new LimitLongOperation(-1);
   }
}
