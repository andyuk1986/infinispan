package org.infinispan.stream.impl.intops.primitive.i;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.annotations.Test;

/**
 * Tests for int stream intermediate operations.
 */
@Test(groups = "unit", testName = "stream.impl.intops.primitive.i.IntStreamOpsTest")
public class IntStreamOpsTest {

   // --- MapIntOperation ---

   public void testMapIntPerform() {
      MapIntOperation op = new MapIntOperation(x -> x * 2);
      int[] result = op.perform(IntStream.of(1, 2, 3)).toArray();
      assertArrayEquals(new int[]{2, 4, 6}, result);
   }

   public void testMapIntMapFlowable() {
      MapIntOperation op = new MapIntOperation(x -> x + 10);
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 3))).toList().blockingGet();
      assertEquals(Arrays.asList(11, 12, 13), result);
   }

   // --- MapToObjIntOperation ---

   public void testMapToObjIntPerform() {
      MapToObjIntOperation<String> op = new MapToObjIntOperation<>(String::valueOf);
      List<String> result = op.perform(IntStream.of(1, 2, 3)).collect(Collectors.toList());
      assertEquals(Arrays.asList("1", "2", "3"), result);
   }

   public void testMapToObjIntMapFlowable() {
      MapToObjIntOperation<String> op = new MapToObjIntOperation<>(i -> "v" + i);
      List<String> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList("v1", "v2"), result);
   }

   // --- MapToDoubleIntOperation ---

   public void testMapToDoubleIntPerform() {
      MapToDoubleIntOperation op = new MapToDoubleIntOperation(i -> i * 1.5);
      double[] result = op.perform(IntStream.of(2, 4)).toArray();
      assertEquals(3.0, result[0], 0.001);
      assertEquals(6.0, result[1], 0.001);
   }

   public void testMapToDoubleIntMapFlowable() {
      MapToDoubleIntOperation op = new MapToDoubleIntOperation(i -> i + 0.5);
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2))).toList().blockingGet();
      assertEquals(1.5, result.get(0), 0.001);
      assertEquals(2.5, result.get(1), 0.001);
   }

   // --- MapToLongIntOperation ---

   public void testMapToLongIntPerform() {
      MapToLongIntOperation op = new MapToLongIntOperation(i -> (long) i * 1000000000L);
      long[] result = op.perform(IntStream.of(1, 2)).toArray();
      assertEquals(1000000000L, result[0]);
      assertEquals(2000000000L, result[1]);
   }

   public void testMapToLongIntMapFlowable() {
      MapToLongIntOperation op = new MapToLongIntOperation(i -> (long) i * 100);
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(100L, 200L), result);
   }

   // --- DistinctIntOperation ---

   public void testDistinctIntPerform() {
      DistinctIntOperation op = DistinctIntOperation.getInstance();
      int[] result = op.perform(IntStream.of(1, 2, 1, 3, 2)).toArray();
      assertArrayEquals(new int[]{1, 2, 3}, result);
   }

   public void testDistinctIntSingleton() {
      assertTrue(DistinctIntOperation.getInstance() == DistinctIntOperation.getInstance());
   }

   public void testDistinctIntMapFlowable() {
      DistinctIntOperation op = DistinctIntOperation.getInstance();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 1, 3))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   // --- AsLongIntOperation ---

   public void testAsLongIntPerform() {
      AsLongIntOperation op = AsLongIntOperation.getInstance();
      long[] result = op.perform(IntStream.of(1, 2, 3)).toArray();
      assertArrayEquals(new long[]{1L, 2L, 3L}, result);
   }

   public void testAsLongIntMapFlowable() {
      AsLongIntOperation op = AsLongIntOperation.getInstance();
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(1L, 2L), result);
   }

   public void testAsLongIntSingleton() {
      assertTrue(AsLongIntOperation.getInstance() == AsLongIntOperation.getInstance());
   }

   // --- AsDoubleIntOperation ---

   public void testAsDoubleIntPerform() {
      AsDoubleIntOperation op = AsDoubleIntOperation.getInstance();
      double[] result = op.perform(IntStream.of(1, 2)).toArray();
      assertEquals(1.0, result[0], 0.001);
      assertEquals(2.0, result[1], 0.001);
   }

   public void testAsDoubleIntMapFlowable() {
      AsDoubleIntOperation op = AsDoubleIntOperation.getInstance();
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(5, 10))).toList().blockingGet();
      assertEquals(Arrays.asList(5.0, 10.0), result);
   }

   public void testAsDoubleIntSingleton() {
      assertTrue(AsDoubleIntOperation.getInstance() == AsDoubleIntOperation.getInstance());
   }

   // --- BoxedIntOperation ---

   public void testBoxedIntPerform() {
      BoxedIntOperation op = BoxedIntOperation.getInstance();
      List<Integer> result = op.perform(IntStream.of(1, 2, 3)).collect(Collectors.toList());
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   public void testBoxedIntMapFlowable() {
      BoxedIntOperation op = BoxedIntOperation.getInstance();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2), result);
   }

   public void testBoxedIntSingleton() {
      assertTrue(BoxedIntOperation.getInstance() == BoxedIntOperation.getInstance());
   }

   // --- SortedIntOperation ---

   public void testSortedIntPerform() {
      SortedIntOperation op = SortedIntOperation.getInstance();
      int[] result = op.perform(IntStream.of(3, 1, 2)).toArray();
      assertArrayEquals(new int[]{1, 2, 3}, result);
   }

   public void testSortedIntSingleton() {
      assertTrue(SortedIntOperation.getInstance() == SortedIntOperation.getInstance());
   }

   public void testSortedIntMapFlowable() {
      SortedIntOperation op = SortedIntOperation.getInstance();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(3, 1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   // --- LimitIntOperation ---

   public void testLimitIntPerform() {
      LimitIntOperation op = new LimitIntOperation(2);
      int[] result = op.perform(IntStream.of(1, 2, 3, 4)).toArray();
      assertArrayEquals(new int[]{1, 2}, result);
   }

   public void testLimitIntMapFlowable() {
      LimitIntOperation op = new LimitIntOperation(3);
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(10, 20, 30, 40, 50))).toList().blockingGet();
      assertEquals(Arrays.asList(10, 20, 30), result);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitIntZero() {
      new LimitIntOperation(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitIntNegative() {
      new LimitIntOperation(-5);
   }
}
