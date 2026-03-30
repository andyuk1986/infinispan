package org.infinispan.stream.impl.spliterators;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.Closeables;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "stream.impl.spliterators.IteratorAsSpliteratorTest")
public class IteratorAsSpliteratorTest extends AbstractInfinispanTest {

   public void testTryAdvanceWithElements() {
      List<String> items = Arrays.asList("a", "b", "c");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator()).get();

      List<String> result = new ArrayList<>();
      assertTrue(spliterator.tryAdvance(result::add));
      assertEquals("a", result.get(0));
      assertTrue(spliterator.tryAdvance(result::add));
      assertEquals("b", result.get(1));
      assertTrue(spliterator.tryAdvance(result::add));
      assertEquals("c", result.get(2));
      assertFalse(spliterator.tryAdvance(result::add));
   }

   public void testTryAdvanceNullAction() {
      List<String> items = Arrays.asList("a");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator()).get();
      try {
         spliterator.tryAdvance(null);
         assert false : "Expected NullPointerException";
      } catch (NullPointerException expected) {
      }
   }

   public void testTryAdvanceEmptyIterator() {
      List<String> items = List.of();
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator()).get();
      assertFalse(spliterator.tryAdvance(v -> {}));
   }

   public void testForEachRemaining() {
      List<String> items = Arrays.asList("x", "y", "z");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator()).get();

      List<String> result = new ArrayList<>();
      spliterator.forEachRemaining(result::add);
      assertEquals(items, result);
   }

   public void testForEachRemainingNullAction() {
      List<String> items = Arrays.asList("a");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator()).get();
      try {
         spliterator.forEachRemaining(null);
         assert false : "Expected NullPointerException";
      } catch (NullPointerException expected) {
      }
   }

   public void testTrySplitWithElements() {
      List<Integer> items = new ArrayList<>();
      for (int i = 0; i < 5000; i++) {
         items.add(i);
      }
      IteratorAsSpliterator<Integer> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setBatchIncrease(100)
            .setMaxBatchSize(500)
            .setEstimateRemaining(5000)
            .get();

      Spliterator<Integer> split = spliterator.trySplit();
      assertNotNull(split);
   }

   public void testTrySplitEmptyIterator() {
      List<String> items = List.of();
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(0)
            .get();

      assertNull(spliterator.trySplit());
   }

   public void testTrySplitEstimateOne() {
      List<String> items = Arrays.asList("only");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(1)
            .get();

      assertNull(spliterator.trySplit());
   }

   public void testTrySplitBatchLargerThanEstimate() {
      List<Integer> items = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
         items.add(i);
      }
      IteratorAsSpliterator<Integer> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setBatchIncrease(5000)
            .setMaxBatchSize(50000)
            .setEstimateRemaining(50)
            .get();

      Spliterator<Integer> split = spliterator.trySplit();
      assertNotNull(split);
   }

   public void testTrySplitBatchExceedsMaxBatchSize() {
      List<Integer> items = new ArrayList<>();
      for (int i = 0; i < 2000; i++) {
         items.add(i);
      }
      // batchIncrease=200, maxBatchSize=200: first split gets 200, second split
      // would try currentBatchSize(200)+batchIncrease(200)=400, capped to maxBatchSize=200
      IteratorAsSpliterator<Integer> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setBatchIncrease(200)
            .setMaxBatchSize(200)
            .setEstimateRemaining(2000)
            .get();

      Spliterator<Integer> split1 = spliterator.trySplit();
      assertNotNull(split1);
      // Second split triggers the batch > maxBatchSize branch
      Spliterator<Integer> split2 = spliterator.trySplit();
      assertNotNull(split2);
   }

   public void testEstimateSize() {
      List<String> items = Arrays.asList("a", "b");
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(items.iterator())
            .setEstimateRemaining(42)
            .get();

      assertEquals(42, spliterator.estimateSize());
   }

   public void testCharacteristics() {
      int chars = Spliterator.ORDERED | Spliterator.NONNULL;
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(List.of("a").iterator())
            .setCharacteristics(chars)
            .get();

      assertEquals(chars, spliterator.characteristics());
   }

   public void testGetComparatorWhenSorted() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(List.of("a").iterator())
            .setCharacteristics(Spliterator.SORTED)
            .get();

      assertNull(spliterator.getComparator());
   }

   public void testGetComparatorWhenNotSorted() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(List.of("a").iterator())
            .setCharacteristics(Spliterator.ORDERED)
            .get();

      try {
         spliterator.getComparator();
         assert false : "Expected IllegalStateException";
      } catch (IllegalStateException expected) {
      }
   }

   public void testClose() {
      boolean[] closed = {false};
      CloseableIterator<String> closeableIterator = new CloseableIterator<>() {
         private final Iterator<String> delegate = List.of("a").iterator();

         @Override
         public void close() {
            closed[0] = true;
         }

         @Override
         public boolean hasNext() {
            return delegate.hasNext();
         }

         @Override
         public String next() {
            return delegate.next();
         }
      };

      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(closeableIterator).get();
      spliterator.close();
      assertTrue(closed[0]);
   }

   public void testBuilderWithCloseableIterator() {
      CloseableIterator<String> iter = Closeables.iterator(List.of("a", "b").iterator());
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(iter).get();
      assertNotNull(spliterator);
   }

   public void testBuilderNullIterator() {
      try {
         new IteratorAsSpliterator.Builder<>((Iterator<String>) null);
         assert false : "Expected NullPointerException";
      } catch (NullPointerException expected) {
      }
   }

   public void testBuilderNullCloseableIterator() {
      try {
         new IteratorAsSpliterator.Builder<>((CloseableIterator<String>) null);
         assert false : "Expected NullPointerException";
      } catch (NullPointerException expected) {
      }
   }

   public void testBuilderInvalidBatchIncrease() {
      try {
         new IteratorAsSpliterator.Builder<>(List.of("a").iterator()).setBatchIncrease(0);
         assert false : "Expected IllegalArgumentException";
      } catch (IllegalArgumentException expected) {
      }

      try {
         new IteratorAsSpliterator.Builder<>(List.of("a").iterator()).setBatchIncrease(-1);
         assert false : "Expected IllegalArgumentException";
      } catch (IllegalArgumentException expected) {
      }
   }

   public void testBuilderInvalidMaxBatchSize() {
      try {
         new IteratorAsSpliterator.Builder<>(List.of("a").iterator()).setMaxBatchSize(0);
         assert false : "Expected IllegalArgumentException";
      } catch (IllegalArgumentException expected) {
      }

      try {
         new IteratorAsSpliterator.Builder<>(List.of("a").iterator()).setMaxBatchSize(-5);
         assert false : "Expected IllegalArgumentException";
      } catch (IllegalArgumentException expected) {
      }
   }

   public void testBuilderBatchIncreaseLargerThanMaxBatchSize() {
      try {
         new IteratorAsSpliterator.Builder<>(List.of("a").iterator())
               .setBatchIncrease(100)
               .setMaxBatchSize(50)
               .get();
         assert false : "Expected IllegalArgumentException";
      } catch (IllegalArgumentException expected) {
      }
   }

   public void testDefaultEstimateRemaining() {
      IteratorAsSpliterator<String> spliterator = new IteratorAsSpliterator.Builder<>(List.of("a").iterator()).get();
      assertEquals(Long.MAX_VALUE, spliterator.estimateSize());
   }
}
