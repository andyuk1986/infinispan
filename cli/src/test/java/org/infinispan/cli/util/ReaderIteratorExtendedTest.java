package org.infinispan.cli.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @since 16.2
 */
public class ReaderIteratorExtendedTest {

   @Test
   public void testSimpleLineReading() {
      String input = "line1\nline2\nline3\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      ReaderIterator it = new ReaderIterator(is, null);

      assertTrue(it.hasNext());
      assertEquals("line1", it.next());
      assertTrue(it.hasNext());
      assertEquals("line2", it.next());
      assertTrue(it.hasNext());
      assertEquals("line3", it.next());
      assertFalse(it.hasNext());
   }

   @Test
   public void testEmptyStream() {
      InputStream is = new ByteArrayInputStream(new byte[0]);
      ReaderIterator it = new ReaderIterator(is, null);

      assertFalse(it.hasNext());
   }

   @Test
   public void testRegexMode() {
      String input = "key1=value1 key2=value2\nkey3=value3\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      Pattern regex = Pattern.compile("(\\w+=\\w+)");
      ReaderIterator it = new ReaderIterator(is, regex);

      List<String> results = new ArrayList<>();
      while (it.hasNext()) {
         results.add(it.next());
      }

      assertEquals(3, results.size());
      assertEquals("key1=value1", results.get(0));
      assertEquals("key2=value2", results.get(1));
      assertEquals("key3=value3", results.get(2));
   }

   @Test
   public void testCloseIdempotent() {
      String input = "line1\nline2\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      ReaderIterator it = new ReaderIterator(is, null);

      it.close();
      it.close(); // Should not throw
      assertFalse(it.hasNext());
   }

   @Test(expected = NoSuchElementException.class)
   public void testNextAfterExhausted() {
      String input = "line1\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      ReaderIterator it = new ReaderIterator(is, null);

      it.next(); // consume
      it.next(); // should throw
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testRemoveThrows() {
      String input = "line1\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      ReaderIterator it = new ReaderIterator(is, null);

      it.remove();
   }

   @Test
   public void testHasNextIdempotent() {
      String input = "line1\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      ReaderIterator it = new ReaderIterator(is, null);

      assertTrue(it.hasNext());
      assertTrue(it.hasNext()); // Multiple calls should not advance
      assertEquals("line1", it.next());
      assertFalse(it.hasNext());
   }

   @Test
   public void testRegexNoMatchLine() {
      String input = "no-match-here\nfound=(value)\n";
      InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
      Pattern regex = Pattern.compile("found=\\((\\w+)\\)");
      ReaderIterator it = new ReaderIterator(is, regex);

      assertTrue(it.hasNext());
      assertEquals("value", it.next());
      assertFalse(it.hasNext());
   }
}
