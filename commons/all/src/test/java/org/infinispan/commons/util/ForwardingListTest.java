package org.infinispan.commons.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.Test;

/**
 * Tests for {@link ForwardingList}.
 */
public class ForwardingListTest {

   private ForwardingList<String> create(List<String> delegate) {
      return new ForwardingList<>() {
         @Override
         protected List<String> delegate() {
            return delegate;
         }
      };
   }

   @Test
   public void testSize() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      assertEquals(3, list.size());
   }

   @Test
   public void testIsEmpty() {
      assertTrue(create(new ArrayList<>()).isEmpty());
      assertFalse(create(new ArrayList<>(Collections.singletonList("x"))).isEmpty());
   }

   @Test
   public void testGet() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      assertEquals("a", list.get(0));
      assertEquals("b", list.get(1));
   }

   @Test
   public void testSet() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      String old = list.set(0, "x");
      assertEquals("a", old);
      assertEquals("x", list.get(0));
   }

   @Test
   public void testAddAtIndex() {
      List<String> backing = new ArrayList<>(Arrays.asList("a", "c"));
      ForwardingList<String> list = create(backing);
      list.add(1, "b");
      assertEquals(3, list.size());
      assertEquals("b", list.get(1));
   }

   @Test
   public void testAddElement() {
      ForwardingList<String> list = create(new ArrayList<>());
      assertTrue(list.add("x"));
      assertEquals(1, list.size());
      assertEquals("x", list.get(0));
   }

   @Test
   public void testAddAllAtIndex() {
      List<String> backing = new ArrayList<>(Arrays.asList("a", "d"));
      ForwardingList<String> list = create(backing);
      assertTrue(list.addAll(1, Arrays.asList("b", "c")));
      assertEquals(4, list.size());
      assertEquals("b", list.get(1));
      assertEquals("c", list.get(2));
   }

   @Test
   public void testAddAll() {
      ForwardingList<String> list = create(new ArrayList<>());
      assertTrue(list.addAll(Arrays.asList("a", "b")));
      assertEquals(2, list.size());
   }

   @Test
   public void testRemoveByIndex() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      String removed = list.remove(1);
      assertEquals("b", removed);
      assertEquals(2, list.size());
   }

   @Test
   public void testRemoveByObject() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      assertTrue(list.remove("b"));
      assertEquals(2, list.size());
      assertFalse(list.contains("b"));
   }

   @Test
   public void testContains() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      assertTrue(list.contains("a"));
      assertFalse(list.contains("z"));
   }

   @Test
   public void testContainsAll() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      assertTrue(list.containsAll(Arrays.asList("a", "c")));
      assertFalse(list.containsAll(Arrays.asList("a", "z")));
   }

   @Test
   public void testIndexOf() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "a")));
      assertEquals(0, list.indexOf("a"));
      assertEquals(-1, list.indexOf("z"));
   }

   @Test
   public void testLastIndexOf() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "a")));
      assertEquals(2, list.lastIndexOf("a"));
   }

   @Test
   public void testIterator() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("x", "y")));
      Iterator<String> it = list.iterator();
      assertTrue(it.hasNext());
      assertEquals("x", it.next());
      assertEquals("y", it.next());
      assertFalse(it.hasNext());
   }

   @Test
   public void testListIterator() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      ListIterator<String> it = list.listIterator();
      assertEquals("a", it.next());
      assertEquals("b", it.next());
   }

   @Test
   public void testListIteratorAtIndex() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      ListIterator<String> it = list.listIterator(1);
      assertEquals("b", it.next());
   }

   @Test
   public void testSubList() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c", "d")));
      List<String> sub = list.subList(1, 3);
      assertEquals(2, sub.size());
      assertEquals("b", sub.get(0));
      assertEquals("c", sub.get(1));
   }

   @Test
   public void testToArray() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      Object[] arr = list.toArray();
      assertArrayEquals(new Object[]{"a", "b"}, arr);
   }

   @Test
   public void testToArrayTyped() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      String[] arr = list.toArray(new String[0]);
      assertArrayEquals(new String[]{"a", "b"}, arr);
   }

   @Test
   public void testRemoveAll() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      assertTrue(list.removeAll(Arrays.asList("a", "c")));
      assertEquals(1, list.size());
      assertEquals("b", list.get(0));
   }

   @Test
   public void testRetainAll() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b", "c")));
      assertTrue(list.retainAll(Arrays.asList("b")));
      assertEquals(1, list.size());
      assertEquals("b", list.get(0));
   }

   @Test
   public void testClear() {
      ForwardingList<String> list = create(new ArrayList<>(Arrays.asList("a", "b")));
      list.clear();
      assertTrue(list.isEmpty());
   }

   @Test
   public void testEqualsDelegate() {
      List<String> backing = new ArrayList<>(Arrays.asList("a", "b"));
      ForwardingList<String> list = create(backing);
      assertTrue(list.equals(backing));
      assertTrue(list.equals(list));
   }

   @Test
   public void testHashCodeDelegate() {
      List<String> backing = new ArrayList<>(Arrays.asList("a", "b"));
      ForwardingList<String> list = create(backing);
      assertEquals(backing.hashCode(), list.hashCode());
   }
}
