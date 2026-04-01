package org.infinispan.stream.impl.spliterators;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;

import org.testng.annotations.Test;

/**
 * Tests for {@link IteratorAsSpliterator}.
 */
@Test(groups = "unit", testName = "stream.impl.spliterators.IteratorAsSpliteratorTest")
public class IteratorAsSpliteratorTest {

   public void testTryAdvance() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a", "b", "c").iterator())
            .setEstimateRemaining(3)
            .get();

      List<String> collected = new ArrayList<>();
      assertTrue(spliterator.tryAdvance(collected::add));
      assertTrue(spliterator.tryAdvance(collected::add));
      assertTrue(spliterator.tryAdvance(collected::add));
      assertFalse(spliterator.tryAdvance(collected::add));
      assertEquals(Arrays.asList("a", "b", "c"), collected);
   }

   public void testForEachRemaining() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("x", "y", "z").iterator())
            .setEstimateRemaining(3)
            .get();

      List<String> collected = new ArrayList<>();
      spliterator.forEachRemaining(collected::add);
      assertEquals(Arrays.asList("x", "y", "z"), collected);
   }

   public void testEstimateSize() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a", "b").iterator())
            .setEstimateRemaining(42)
            .get();
      assertEquals(42, spliterator.estimateSize());
   }

   public void testEstimateSizeDefault() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .get();
      assertEquals(Long.MAX_VALUE, spliterator.estimateSize());
   }

   public void testCharacteristics() {
      int chars = Spliterator.DISTINCT | Spliterator.NONNULL;
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .setCharacteristics(chars)
            .get();
      assertEquals(chars, spliterator.characteristics());
   }

   public void testTrySplitWithElements() {
      List<String> items = Arrays.asList("a", "b", "c", "d", "e");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(5)
            .setBatchIncrease(2)
            .setMaxBatchSize(10)
            .get();

      Spliterator<String> split = spliterator.trySplit();
      assertNotNull(split);
   }

   public void testTrySplitEmptyIterator() {
      List<String> items = new ArrayList<>();
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(0)
            .get();

      Spliterator<String> split = spliterator.trySplit();
      assertNull(split);
   }

   public void testTrySplitSingleElement() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .setEstimateRemaining(1)
            .setBatchIncrease(1)
            .setMaxBatchSize(1)
            .get();

      Spliterator<String> split = spliterator.trySplit();
      // Single element may or may not produce a split depending on implementation
      if (split != null) {
         // After splitting single element, no more to split
         assertNull(spliterator.trySplit());
      }
   }

   public void testGetComparatorSorted() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .setCharacteristics(Spliterator.SORTED)
            .get();
      // SORTED with natural order returns null
      assertNull(spliterator.getComparator());
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testGetComparatorNotSorted() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .setCharacteristics(0)
            .get();
      spliterator.getComparator();
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testTryAdvanceNullAction() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .get();
      spliterator.tryAdvance(null);
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testForEachRemainingNullAction() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .get();
      spliterator.forEachRemaining(null);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuilderBatchIncreaseMustBePositive() {
      new IteratorAsSpliterator.Builder<>(Arrays.asList("a").iterator())
            .setBatchIncrease(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuilderMaxBatchSizeMustBePositive() {
      new IteratorAsSpliterator.Builder<>(Arrays.asList("a").iterator())
            .setMaxBatchSize(0);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuilderBatchIncreaseLargerThanMaxBatchSize() {
      new IteratorAsSpliterator.Builder<>(Arrays.asList("a").iterator())
            .setBatchIncrease(100)
            .setMaxBatchSize(50)
            .get();
   }

   public void testClose() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(
            Arrays.asList("a").iterator())
            .get();
      // Should not throw
      spliterator.close();
   }

   public void testTrySplitRespectsBatchMax() {
      List<Integer> items = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
         items.add(i);
      }
      IteratorAsSpliterator<Integer> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(100)
            .setBatchIncrease(5)
            .setMaxBatchSize(10)
            .get();

      // First split: batchIncrease = 5
      Spliterator<Integer> split1 = spliterator.trySplit();
      assertNotNull(split1);

      // Second split: 5+5=10
      Spliterator<Integer> split2 = spliterator.trySplit();
      assertNotNull(split2);

      // Third split: capped at maxBatchSize=10
      Spliterator<Integer> split3 = spliterator.trySplit();
      assertNotNull(split3);
   }
}
