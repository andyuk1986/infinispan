package org.infinispan.cli.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @since 16.2
 */
public class TransformingIteratorTest {

   @Test
   public void testHasNextAndNext() {
      Iterator<Integer> source = List.of(1, 2, 3).iterator();
      TransformingIterator<Integer, String> it = new TransformingIterator<>(source, String::valueOf);

      assertTrue(it.hasNext());
      assertEquals("1", it.next());
      assertTrue(it.hasNext());
      assertEquals("2", it.next());
      assertTrue(it.hasNext());
      assertEquals("3", it.next());
      assertFalse(it.hasNext());
   }

   @Test
   public void testEmptyIterator() {
      Iterator<Integer> source = List.<Integer>of().iterator();
      TransformingIterator<Integer, String> it = new TransformingIterator<>(source, String::valueOf);

      assertFalse(it.hasNext());
   }

   @Test(expected = NoSuchElementException.class)
   public void testNextOnExhaustedIterator() {
      Iterator<Integer> source = List.of(1).iterator();
      TransformingIterator<Integer, String> it = new TransformingIterator<>(source, String::valueOf);

      it.next(); // consume single element
      it.next(); // should throw
   }

   @Test
   public void testTransformFunction() {
      Iterator<String> source = List.of("hello", "world").iterator();
      TransformingIterator<String, Integer> it = new TransformingIterator<>(source, String::length);

      assertEquals(Integer.valueOf(5), it.next());
      assertEquals(Integer.valueOf(5), it.next());
      assertFalse(it.hasNext());
   }
}
