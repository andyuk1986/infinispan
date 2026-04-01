package org.infinispan.cli.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @since 16.2
 */
public class JsonReaderIteratorExtendedTest {

   @Test
   public void testEmptyArray() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[]"));
      assertFalse(it.hasNext());
   }

   @Test
   public void testSingleBareValue() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[\"hello\"]"));
      assertTrue(it.hasNext());
      Map<String, String> row = it.next();
      assertEquals("hello", row.get(""));
      assertFalse(it.hasNext());
   }

   @Test
   public void testSimpleObject() throws IOException {
      String json = "[{\"name\": \"John\", \"age\": \"30\"}]";
      JsonReaderIterator it = new JsonReaderIterator(new StringReader(json));
      assertTrue(it.hasNext());
      Map<String, String> row = it.next();
      assertEquals("John", row.get("name"));
      assertEquals("30", row.get("age"));
      assertFalse(it.hasNext());
   }

   @Test
   public void testNestedObjectWithTypeAndValue() throws IOException {
      String json = """
            [{"key": {"_type": "string", "_value": "myKey"}, "val": "myVal"}]""";
      JsonReaderIterator it = new JsonReaderIterator(new StringReader(json));
      assertTrue(it.hasNext());
      Map<String, String> row = it.next();
      assertEquals("myKey", row.get("key"));
      assertEquals("myVal", row.get("val"));
      assertFalse(it.hasNext());
   }

   @Test(expected = NoSuchElementException.class)
   public void testNextOnExhausted() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[]"));
      assertFalse(it.hasNext());
      it.next(); // should throw
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testRemoveThrows() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[\"a\"]"));
      it.remove();
   }

   @Test
   public void testCloseIdempotent() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[\"a\"]"));
      it.close();
      it.close(); // Should not throw
      assertFalse(it.hasNext());
   }

   @Test
   public void testMultipleObjects() throws IOException {
      String json = """
            [{"a": "1"}, {"a": "2"}, {"a": "3"}]""";
      JsonReaderIterator it = new JsonReaderIterator(new StringReader(json));

      assertTrue(it.hasNext());
      assertEquals("1", it.next().get("a"));
      assertTrue(it.hasNext());
      assertEquals("2", it.next().get("a"));
      assertTrue(it.hasNext());
      assertEquals("3", it.next().get("a"));
      assertFalse(it.hasNext());
   }

   @Test
   public void testHasNextIdempotent() throws IOException {
      JsonReaderIterator it = new JsonReaderIterator(new StringReader("[\"x\"]"));
      assertTrue(it.hasNext());
      assertTrue(it.hasNext()); // Should not advance
      assertEquals("x", it.next().get(""));
      assertFalse(it.hasNext());
   }
}
