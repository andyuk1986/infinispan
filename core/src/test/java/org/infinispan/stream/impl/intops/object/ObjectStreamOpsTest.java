package org.infinispan.stream.impl.intops.object;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.Test;

/**
 * Tests for object stream intermediate operations.
 */
@Test(groups = "unit", testName = "stream.impl.intops.object.ObjectStreamOpsTest")
public class ObjectStreamOpsTest {

   // --- DistinctOperation ---

   public void testDistinctPerform() {
      DistinctOperation<String> op = DistinctOperation.getInstance();
      List<String> result = op.perform(Stream.of("a", "b", "a", "c", "b"))
            .collect(Collectors.toList());
      assertEquals(3, result.size());
      assertTrue(result.contains("a"));
      assertTrue(result.contains("b"));
      assertTrue(result.contains("c"));
   }

   public void testDistinctSingleton() {
      DistinctOperation<String> op1 = DistinctOperation.getInstance();
      DistinctOperation<String> op2 = DistinctOperation.getInstance();
      assertTrue(op1 == op2);
   }

   public void testDistinctMapFlowable() {
      DistinctOperation<Integer> op = DistinctOperation.getInstance();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 1, 3, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   // --- SortedOperation ---

   public void testSortedPerform() {
      SortedOperation<String> op = SortedOperation.getInstance();
      List<String> result = op.perform(Stream.of("c", "a", "b"))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList("a", "b", "c"), result);
   }

   public void testSortedSingleton() {
      SortedOperation<String> op1 = SortedOperation.getInstance();
      SortedOperation<String> op2 = SortedOperation.getInstance();
      assertTrue(op1 == op2);
   }

   public void testSortedMapFlowable() {
      SortedOperation<Integer> op = SortedOperation.getInstance();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(3, 1, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   // --- SortedComparatorOperation ---

   public void testSortedComparatorPerform() {
      Comparator<String> comp = Comparator.reverseOrder();
      SortedComparatorOperation<String> op = new SortedComparatorOperation<>(comp);
      List<String> result = op.perform(Stream.of("a", "c", "b"))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList("c", "b", "a"), result);
   }

   public void testSortedComparatorGetComparator() {
      Comparator<String> comp = Comparator.reverseOrder();
      SortedComparatorOperation<String> op = new SortedComparatorOperation<>(comp);
      assertEquals(comp, op.getComparator());
   }

   public void testSortedComparatorMapFlowable() {
      Comparator<Integer> comp = Comparator.reverseOrder();
      SortedComparatorOperation<Integer> op = new SortedComparatorOperation<>(comp);
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 3, 2))).toList().blockingGet();
      assertEquals(Arrays.asList(3, 2, 1), result);
   }

   // --- FilterOperation ---

   public void testFilterPerform() {
      FilterOperation<Integer> op = new FilterOperation<>(x -> x > 2);
      List<Integer> result = op.perform(Stream.of(1, 2, 3, 4, 5))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList(3, 4, 5), result);
   }

   public void testFilterMapFlowable() {
      FilterOperation<String> op = new FilterOperation<>(s -> s.startsWith("a"));
      List<String> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList("apple", "banana", "avocado"))).toList().blockingGet();
      assertEquals(Arrays.asList("apple", "avocado"), result);
   }

   public void testFilterToString() {
      FilterOperation<String> op = new FilterOperation<>(s -> true);
      assertTrue(op.toString().contains("FilterOperation"));
   }

   // --- SkipOperation ---

   public void testSkipPerform() {
      SkipOperation<String> op = new SkipOperation<>(2);
      List<String> result = op.perform(Stream.of("a", "b", "c", "d"))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList("c", "d"), result);
   }

   public void testSkipMapFlowable() {
      SkipOperation<Integer> op = new SkipOperation<>(3);
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 3, 4, 5))).toList().blockingGet();
      assertEquals(Arrays.asList(4, 5), result);
   }

   // --- LimitOperation ---

   public void testLimitPerform() {
      LimitOperation<String> op = new LimitOperation<>(2);
      List<String> result = op.perform(Stream.of("a", "b", "c", "d"))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList("a", "b"), result);
   }

   public void testLimitMapFlowable() {
      LimitOperation<Integer> op = new LimitOperation<>(3);
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 3, 4, 5))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitZero() {
      new LimitOperation<>(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testLimitNegative() {
      new LimitOperation<>(-1);
   }

   // --- FlatMapOperation ---

   public void testFlatMapPerform() {
      FlatMapOperation<String, Character> op = new FlatMapOperation<>(
            s -> s.chars().mapToObj(c -> (char) c));
      List<Character> result = op.perform(Stream.of("ab", "cd"))
            .collect(Collectors.toList());
      assertEquals(Arrays.asList('a', 'b', 'c', 'd'), result);
   }

   public void testFlatMapMapFlowable() {
      FlatMapOperation<String, String> op = new FlatMapOperation<>(
            s -> Stream.of(s, s.toUpperCase()));
      List<String> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList("a", "b"))).toList().blockingGet();
      assertEquals(Arrays.asList("a", "A", "b", "B"), result);
   }

   // --- FlatMapToIntOperation ---

   public void testFlatMapToIntPerform() {
      FlatMapToIntOperation<String> op = new FlatMapToIntOperation<>(
            s -> s.chars());
      int[] result = op.perform(Stream.of("ab")).toArray();
      assertEquals(2, result.length);
      assertEquals('a', result[0]);
      assertEquals('b', result[1]);
   }

   // --- FlatMapToLongOperation ---

   public void testFlatMapToLongPerform() {
      FlatMapToLongOperation<int[]> op = new FlatMapToLongOperation<>(
            arr -> Arrays.stream(arr).asLongStream());
      long[] result = op.perform(Stream.of(new int[]{1, 2})).toArray();
      assertEquals(2, result.length);
      assertEquals(1L, result[0]);
      assertEquals(2L, result[1]);
   }

   // --- FlatMapToDoubleOperation ---

   public void testFlatMapToDoublePerform() {
      FlatMapToDoubleOperation<int[]> op = new FlatMapToDoubleOperation<>(
            arr -> Arrays.stream(arr).asDoubleStream());
      double[] result = op.perform(Stream.of(new int[]{1, 2})).toArray();
      assertEquals(2, result.length);
   }
}
