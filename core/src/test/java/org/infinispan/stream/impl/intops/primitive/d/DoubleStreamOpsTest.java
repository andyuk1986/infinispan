package org.infinispan.stream.impl.intops.primitive.d;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.testng.annotations.Test;

/**
 * Tests for double stream intermediate operations.
 */
@Test(groups = "unit", testName = "stream.impl.intops.primitive.d.DoubleStreamOpsTest")
public class DoubleStreamOpsTest {

   // --- MapDoubleOperation ---

   public void testMapDoublePerform() {
      MapDoubleOperation op = new MapDoubleOperation(x -> x * 2.0);
      double[] result = op.perform(DoubleStream.of(1.0, 2.5, 3.0)).toArray();
      assertEquals(2.0, result[0], 0.001);
      assertEquals(5.0, result[1], 0.001);
      assertEquals(6.0, result[2], 0.001);
   }

   public void testMapDoubleMapFlowable() {
      MapDoubleOperation op = new MapDoubleOperation(x -> x + 0.5);
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.0, 2.0))).toList().blockingGet();
      assertEquals(1.5, result.get(0), 0.001);
      assertEquals(2.5, result.get(1), 0.001);
   }

   // --- MapToObjDoubleOperation ---

   public void testMapToObjDoublePerform() {
      MapToObjDoubleOperation<String> op = new MapToObjDoubleOperation<>(String::valueOf);
      List<String> result = op.perform(DoubleStream.of(1.5, 2.5)).collect(Collectors.toList());
      assertEquals(2, result.size());
   }

   public void testMapToObjDoubleMapFlowable() {
      MapToObjDoubleOperation<String> op = new MapToObjDoubleOperation<>(d -> "D" + d);
      List<String> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.0, 2.0))).toList().blockingGet();
      assertEquals(Arrays.asList("D1.0", "D2.0"), result);
   }

   // --- MapToLongDoubleOperation ---

   public void testMapToLongDoublePerform() {
      MapToLongDoubleOperation op = new MapToLongDoubleOperation(d -> (long) d);
      long[] result = op.perform(DoubleStream.of(1.9, 2.1)).toArray();
      assertArrayEquals(new long[]{1L, 2L}, result);
   }

   public void testMapToLongDoubleMapFlowable() {
      MapToLongDoubleOperation op = new MapToLongDoubleOperation(d -> Math.round(d));
      List<Long> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.4, 2.6))).toList().blockingGet();
      assertEquals(Arrays.asList(1L, 3L), result);
   }

   // --- MapToIntDoubleOperation ---

   public void testMapToIntDoublePerform() {
      MapToIntDoubleOperation op = new MapToIntDoubleOperation(d -> (int) d);
      int[] result = op.perform(DoubleStream.of(1.9, 2.1)).toArray();
      assertArrayEquals(new int[]{1, 2}, result);
   }

   public void testMapToIntDoubleMapFlowable() {
      MapToIntDoubleOperation op = new MapToIntDoubleOperation(d -> (int) Math.ceil(d));
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.1, 2.9))).toList().blockingGet();
      assertEquals(Arrays.asList(2, 3), result);
   }

   // --- DistinctDoubleOperation ---

   public void testDistinctDoublePerform() {
      DistinctDoubleOperation op = DistinctDoubleOperation.getInstance();
      double[] result = op.perform(DoubleStream.of(1.0, 2.0, 1.0, 3.0)).toArray();
      assertEquals(3, result.length);
   }

   public void testDistinctDoubleSingleton() {
      assertTrue(DistinctDoubleOperation.getInstance() == DistinctDoubleOperation.getInstance());
   }

   public void testDistinctDoubleMapFlowable() {
      DistinctDoubleOperation op = DistinctDoubleOperation.getInstance();
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.0, 2.0, 1.0))).toList().blockingGet();
      assertEquals(Arrays.asList(1.0, 2.0), result);
   }

   // --- BoxedDoubleOperation ---

   public void testBoxedDoublePerform() {
      BoxedDoubleOperation op = BoxedDoubleOperation.getInstance();
      List<Double> result = op.perform(DoubleStream.of(1.0, 2.0, 3.0)).collect(Collectors.toList());
      assertEquals(Arrays.asList(1.0, 2.0, 3.0), result);
   }

   public void testBoxedDoubleMapFlowable() {
      BoxedDoubleOperation op = BoxedDoubleOperation.getInstance();
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.0, 2.0))).toList().blockingGet();
      assertEquals(Arrays.asList(1.0, 2.0), result);
   }

   public void testBoxedDoubleSingleton() {
      assertTrue(BoxedDoubleOperation.getInstance() == BoxedDoubleOperation.getInstance());
   }

   // --- LimitDoubleOperation ---

   public void testLimitDoublePerform() {
      LimitDoubleOperation op = new LimitDoubleOperation(2);
      double[] result = op.perform(DoubleStream.of(1.0, 2.0, 3.0, 4.0)).toArray();
      assertEquals(2, result.length);
      assertEquals(1.0, result[0], 0.001);
      assertEquals(2.0, result[1], 0.001);
   }

   public void testLimitDoubleMapFlowable() {
      LimitDoubleOperation op = new LimitDoubleOperation(3);
      List<Double> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0))).toList().blockingGet();
      assertEquals(Arrays.asList(1.0, 2.0, 3.0), result);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitDoubleZero() {
      new LimitDoubleOperation(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitDoubleNegative() {
      new LimitDoubleOperation(-1);
   }
}
