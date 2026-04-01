package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;

import org.junit.Test;

/**
 * @since 16.2
 */
public class ParameterizedTypeImplTest {

   @Test
   public void testGetters() {
      Type[] args = new Type[]{String.class};
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, args, null);

      assertEquals(java.util.List.class, pt.getRawType());
      assertNull(pt.getOwnerType());
      assertArrayEquals(args, pt.getActualTypeArguments());
   }

   @Test
   public void testActualTypeArgumentsDefensiveCopy() {
      Type[] args = new Type[]{String.class};
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, args, null);

      Type[] returned = pt.getActualTypeArguments();
      returned[0] = Integer.class; // mutate returned array
      // Original should be unaffected
      assertEquals(String.class, pt.getActualTypeArguments()[0]);
   }

   @Test
   public void testEqualsIdentity() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      assertTrue(pt.equals(pt));
   }

   @Test
   public void testEqualsEquivalent() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      assertTrue(pt1.equals(pt2));
      assertTrue(pt2.equals(pt1));
   }

   @Test
   public void testNotEqualsDifferentRawType() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.Set.class, new Type[]{String.class}, null);
      assertFalse(pt1.equals(pt2));
   }

   @Test
   public void testNotEqualsDifferentArgs() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{Integer.class}, null);
      assertFalse(pt1.equals(pt2));
   }

   @Test
   public void testNotEqualsNonParameterizedType() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      assertFalse(pt.equals("not a type"));
      assertFalse(pt.equals(null));
   }

   @Test
   public void testEqualsWithOwnerType() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.Map.Entry.class, new Type[]{String.class, Integer.class}, java.util.Map.class);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.Map.Entry.class, new Type[]{String.class, Integer.class}, java.util.Map.class);
      assertTrue(pt1.equals(pt2));
   }

   @Test
   public void testNotEqualsWithDifferentOwnerType() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.Map.Entry.class, new Type[]{String.class}, java.util.Map.class);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.Map.Entry.class, new Type[]{String.class}, null);
      assertFalse(pt1.equals(pt2));
   }

   @Test
   public void testHashCodeConsistency() {
      ParameterizedTypeImpl pt1 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      ParameterizedTypeImpl pt2 = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      assertEquals(pt1.hashCode(), pt2.hashCode());
   }

   @Test
   public void testToStringWithArgs() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, new Type[]{String.class}, null);
      String str = pt.toString();
      assertTrue(str.contains("List"));
      assertTrue(str.contains("String"));
   }

   @Test
   public void testToStringNoArgs() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.List.class, new Type[]{}, null);
      String str = pt.toString();
      assertTrue(str.contains("List"));
      assertFalse(str.contains("<"));
   }

   @Test
   public void testToStringMultipleArgs() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(java.util.Map.class, new Type[]{String.class, Integer.class}, null);
      String str = pt.toString();
      assertTrue(str.contains("Map"));
      assertTrue(str.contains("String"));
      assertTrue(str.contains("Integer"));
   }
}
