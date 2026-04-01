package org.infinispan.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link WeakValueHashMap}.
 */
public class WeakValueHashMapTest {

   @Test
   public void testDefaultConstructor() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      assertEquals(0, map.size());
      assertTrue(map.isEmpty());
   }

   @Test
   public void testConstructorWithCapacity() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>(16);
      assertEquals(0, map.size());
   }

   @Test
   public void testConstructorWithCapacityAndLoadFactor() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>(16, 0.5f);
      assertEquals(0, map.size());
   }

   @Test
   public void testConstructorWithMap() {
      Map<String, String> source = new HashMap<>();
      source.put("a", "1");
      source.put("b", "2");
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>(source);
      assertEquals(2, map.size());
      assertEquals("1", map.get("a"));
      assertEquals("2", map.get("b"));
   }

   @Test
   public void testPutAndGet() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      assertNull(map.put("key1", "value1"));
      assertEquals("value1", map.get("key1"));
   }

   @Test
   public void testPutReturnsOldValue() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("key1", "value1");
      String old = map.put("key1", "value2");
      assertEquals("value1", old);
      assertEquals("value2", map.get("key1"));
   }

   @Test
   public void testGetNonExistent() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      assertNull(map.get("noKey"));
   }

   @Test
   public void testContainsKey() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("key1", "value1");
      assertTrue(map.containsKey("key1"));
      assertFalse(map.containsKey("key2"));
   }

   @Test
   public void testRemove() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("key1", "value1");
      String removed = map.remove("key1");
      assertEquals("value1", removed);
      assertNull(map.get("key1"));
      assertEquals(0, map.size());
   }

   @Test
   public void testRemoveNonExistent() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      assertNull(map.remove("noKey"));
   }

   @Test
   public void testSize() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      assertEquals(0, map.size());
      map.put("a", "1");
      assertEquals(1, map.size());
      map.put("b", "2");
      assertEquals(2, map.size());
      map.remove("a");
      assertEquals(1, map.size());
   }

   @Test
   public void testClear() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("a", "1");
      map.put("b", "2");
      map.clear();
      assertEquals(0, map.size());
      assertNull(map.get("a"));
   }

   @Test
   public void testEntrySet() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("a", "1");
      map.put("b", "2");
      Set<Map.Entry<String, String>> entries = map.entrySet();
      assertEquals(2, entries.size());
   }

   @Test
   public void testEntrySetIterator() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("a", "1");
      Set<Map.Entry<String, String>> entries = map.entrySet();
      Iterator<Map.Entry<String, String>> it = entries.iterator();
      assertTrue(it.hasNext());
      Map.Entry<String, String> entry = it.next();
      assertNotNull(entry);
      assertEquals("a", entry.getKey());
      assertEquals("1", entry.getValue());
      assertFalse(it.hasNext());
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testEntrySetIteratorRemoveUnsupported() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("a", "1");
      Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
      it.next();
      it.remove();
   }

   @Test
   public void testToString() {
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>();
      map.put("key", "val");
      String str = map.toString();
      assertNotNull(str);
      assertTrue(str.contains("key"));
   }

   @Test
   public void testMultipleEntries() {
      WeakValueHashMap<Integer, String> map = new WeakValueHashMap<>();
      for (int i = 0; i < 50; i++) {
         map.put(i, "value" + i);
      }
      assertEquals(50, map.size());
      for (int i = 0; i < 50; i++) {
         assertEquals("value" + i, map.get(i));
      }
   }

   @Test
   public void testPutAllViaConstructor() {
      Map<String, String> source = new HashMap<>();
      source.put("x", "10");
      source.put("y", "20");
      source.put("z", "30");
      WeakValueHashMap<String, String> map = new WeakValueHashMap<>(source);
      assertEquals(3, map.size());
      assertTrue(map.containsKey("x"));
      assertTrue(map.containsKey("y"));
      assertTrue(map.containsKey("z"));
   }
}
