package org.infinispan.cli.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @since 16.2
 */
public class TransformingIterableTest {

   @Test
   public void testTransformingIterable() {
      List<Integer> source = List.of(1, 2, 3);
      TransformingIterable<Integer, String> iterable = new TransformingIterable<>(source, String::valueOf);

      List<String> result = new ArrayList<>();
      iterable.iterator().forEachRemaining(result::add);

      assertEquals(List.of("1", "2", "3"), result);
   }

   @Test
   public void testEmptyIterable() {
      List<Integer> source = List.of();
      TransformingIterable<Integer, String> iterable = new TransformingIterable<>(source, String::valueOf);

      Iterator<String> it = iterable.iterator();
      assertFalse(it.hasNext());
   }

   @Test
   public void testSingletonMapValueFunction() {
      List<Map<String, String>> source = List.of(
            Map.of("key1", "value1"),
            Map.of("key2", "value2")
      );
      TransformingIterable<Map<String, String>, String> iterable =
            new TransformingIterable<>(source, TransformingIterable.SINGLETON_MAP_VALUE);

      List<String> result = new ArrayList<>();
      iterable.iterator().forEachRemaining(result::add);

      assertEquals(2, result.size());
      assertTrue(result.contains("value1"));
      assertTrue(result.contains("value2"));
   }

   @Test
   public void testMultipleIterations() {
      List<String> source = List.of("a", "b", "c");
      TransformingIterable<String, String> iterable = new TransformingIterable<>(source, String::toUpperCase);

      // First iteration
      List<String> result1 = new ArrayList<>();
      iterable.iterator().forEachRemaining(result1::add);

      // Second iteration should produce same results
      List<String> result2 = new ArrayList<>();
      iterable.iterator().forEachRemaining(result2::add);

      assertEquals(result1, result2);
      assertEquals(List.of("A", "B", "C"), result1);
   }
}
