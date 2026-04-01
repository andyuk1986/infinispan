package org.infinispan.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link InfinispanCollections}.
 */
public class InfinispanCollectionsTest {

   // --- transformMapValue ---

   @Test
   public void testTransformMapValueEmpty() {
      Map<String, Integer> result = InfinispanCollections.transformMapValue(
            Collections.emptyMap(), v -> 0);
      assertTrue(result.isEmpty());
   }

   @Test
   public void testTransformMapValueSingleEntry() {
      Map<String, String> input = Collections.singletonMap("key", "value");
      Map<String, Integer> result = InfinispanCollections.transformMapValue(input, String::length);
      assertEquals(1, result.size());
      assertEquals(Integer.valueOf(5), result.get("key"));
   }

   @Test
   public void testTransformMapValueMultipleEntries() {
      Map<String, String> input = new HashMap<>();
      input.put("a", "one");
      input.put("b", "two");
      input.put("c", "three");
      Map<String, Integer> result = InfinispanCollections.transformMapValue(input, String::length);
      assertEquals(3, result.size());
      assertEquals(Integer.valueOf(3), result.get("a"));
      assertEquals(Integer.valueOf(3), result.get("b"));
      assertEquals(Integer.valueOf(5), result.get("c"));
   }

   // --- transformCollectionToMap ---

   @Test
   public void testTransformCollectionToMapEmpty() {
      Map<String, Integer> result = InfinispanCollections.transformCollectionToMap(
            Collections.emptyList(),
            s -> new AbstractMap.SimpleEntry<>("k", 1));
      assertTrue(result.isEmpty());
   }

   @Test
   public void testTransformCollectionToMapSingleElement() {
      Map<String, Integer> result = InfinispanCollections.transformCollectionToMap(
            Collections.singletonList("hello"),
            s -> new AbstractMap.SimpleEntry<>(s, s.length()));
      assertEquals(1, result.size());
      assertEquals(Integer.valueOf(5), result.get("hello"));
   }

   @Test
   public void testTransformCollectionToMapMultipleElements() {
      List<String> input = Arrays.asList("a", "bb", "ccc");
      Map<String, Integer> result = InfinispanCollections.transformCollectionToMap(
            input, s -> new AbstractMap.SimpleEntry<>(s, s.length()));
      assertEquals(3, result.size());
      assertEquals(Integer.valueOf(1), result.get("a"));
      assertEquals(Integer.valueOf(2), result.get("bb"));
      assertEquals(Integer.valueOf(3), result.get("ccc"));
   }

   // --- difference ---

   @Test
   public void testDifference() {
      Set<String> s1 = new HashSet<>(Arrays.asList("a", "b", "c"));
      Set<String> s2 = new HashSet<>(Arrays.asList("b", "d"));
      Set<String> diff = InfinispanCollections.difference(s1, s2);
      assertEquals(2, diff.size());
      assertTrue(diff.contains("a"));
      assertTrue(diff.contains("c"));
   }

   @Test
   public void testDifferenceNoOverlap() {
      Set<String> s1 = new HashSet<>(Arrays.asList("a", "b"));
      Set<String> s2 = new HashSet<>(Arrays.asList("c", "d"));
      Set<String> diff = InfinispanCollections.difference(s1, s2);
      assertEquals(s1, diff);
   }

   @Test
   public void testDifferenceSameSet() {
      Set<String> s = new HashSet<>(Arrays.asList("a", "b"));
      Set<String> diff = InfinispanCollections.difference(s, s);
      assertTrue(diff.isEmpty());
   }

   // --- containsAny ---

   @Test
   public void testContainsAnyTrue() {
      List<String> haystack = Arrays.asList("a", "b", "c");
      List<String> needles = Arrays.asList("x", "b");
      assertTrue(InfinispanCollections.containsAny(haystack, needles));
   }

   @Test
   public void testContainsAnyFalse() {
      List<String> haystack = Arrays.asList("a", "b", "c");
      List<String> needles = Arrays.asList("x", "y");
      assertFalse(InfinispanCollections.containsAny(haystack, needles));
   }

   @Test
   public void testContainsAnyEmptyNeedles() {
      List<String> haystack = Arrays.asList("a", "b");
      assertFalse(InfinispanCollections.containsAny(haystack, Collections.emptyList()));
   }

   // --- forEach ---

   @Test
   public void testForEachArray() {
      String[] arr = {"a", "b", "c"};
      List<String> collected = new ArrayList<>();
      InfinispanCollections.forEach(arr, collected::add);
      assertEquals(Arrays.asList("a", "b", "c"), collected);
   }

   @Test(expected = NullPointerException.class)
   public void testForEachNullArray() {
      InfinispanCollections.forEach(null, s -> { });
   }

   // --- assertNotNullEntries Map ---

   @Test
   public void testAssertNotNullEntriesMapValid() {
      Map<String, String> map = new HashMap<>();
      map.put("k", "v");
      InfinispanCollections.assertNotNullEntries(map, "test");
   }

   @Test(expected = NullPointerException.class)
   public void testAssertNotNullEntriesMapNullMap() {
      InfinispanCollections.assertNotNullEntries((Map<?, ?>) null, "test");
   }

   @Test(expected = NullPointerException.class)
   public void testAssertNotNullEntriesMapNullValue() {
      Map<String, String> map = new HashMap<>();
      map.put("k", null);
      InfinispanCollections.assertNotNullEntries(map, "test");
   }

   // --- assertNotNullEntries Collection ---

   @Test
   public void testAssertNotNullEntriesCollectionValid() {
      InfinispanCollections.assertNotNullEntries(Arrays.asList("a", "b"), "test");
   }

   @Test(expected = NullPointerException.class)
   public void testAssertNotNullEntriesCollectionNull() {
      InfinispanCollections.assertNotNullEntries((List<?>) null, "test");
   }

   @Test(expected = NullPointerException.class)
   public void testAssertNotNullEntriesCollectionNullEntry() {
      InfinispanCollections.assertNotNullEntries(Arrays.asList("a", null), "test");
   }

   // --- mergeMaps ---

   @Test
   public void testMergeMapsNullFirst() {
      Map<String, String> second = new HashMap<>();
      second.put("k", "v");
      assertSame(second, InfinispanCollections.mergeMaps(null, second));
   }

   @Test
   public void testMergeMapsNullSecond() {
      Map<String, String> first = new HashMap<>();
      first.put("k", "v");
      assertSame(first, InfinispanCollections.mergeMaps(first, null));
   }

   @Test
   public void testMergeMapsBoth() {
      Map<String, String> one = new HashMap<>();
      one.put("a", "1");
      Map<String, String> two = new HashMap<>();
      two.put("b", "2");
      Map<String, String> result = InfinispanCollections.mergeMaps(one, two);
      assertEquals(2, result.size());
      assertEquals("1", result.get("a"));
      assertEquals("2", result.get("b"));
   }

   // --- mergeLists ---

   @Test
   public void testMergeListsNullFirst() {
      List<String> second = new ArrayList<>(Collections.singletonList("a"));
      assertSame(second, InfinispanCollections.mergeLists(null, second));
   }

   @Test
   public void testMergeListsNullSecond() {
      List<String> first = new ArrayList<>(Collections.singletonList("a"));
      assertSame(first, InfinispanCollections.mergeLists(first, null));
   }

   @Test
   public void testMergeListsBoth() {
      List<String> one = new ArrayList<>(Arrays.asList("a"));
      List<String> two = new ArrayList<>(Arrays.asList("b"));
      List<String> result = InfinispanCollections.mergeLists(one, two);
      assertEquals(2, result.size());
      assertTrue(result.contains("a"));
      assertTrue(result.contains("b"));
   }

   // --- toObjectSet ---

   @Test
   public void testToObjectSetFromSet() {
      Set<String> input = new HashSet<>(Arrays.asList("a", "b"));
      Set<Object> result = InfinispanCollections.toObjectSet(input);
      // If input is already a Set, should return it directly
      assertSame(input, result);
   }

   @Test
   public void testToObjectSetFromList() {
      List<String> input = Arrays.asList("a", "b", "a");
      Set<Object> result = InfinispanCollections.toObjectSet(input);
      assertEquals(2, result.size());
      assertTrue(result.contains("a"));
      assertTrue(result.contains("b"));
   }
}
