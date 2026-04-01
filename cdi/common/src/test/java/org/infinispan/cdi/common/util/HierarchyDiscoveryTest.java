package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

/**
 * @since 16.2
 */
public class HierarchyDiscoveryTest {

   @Test
   public void testSimpleClass() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(String.class);
      Set<Type> types = discovery.getTypeClosure();

      assertNotNull(types);
      assertTrue(types.contains(String.class));
      assertTrue(types.contains(Object.class));
      assertTrue(types.stream().anyMatch(t -> {
         if (t instanceof Class) return ((Class<?>) t) == Serializable.class;
         return false;
      }));
   }

   @Test
   public void testObjectClass() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(Object.class);
      Set<Type> types = discovery.getTypeClosure();

      assertNotNull(types);
      assertTrue(types.contains(Object.class));
   }

   @Test
   public void testInterface() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(Serializable.class);
      Set<Type> types = discovery.getTypeClosure();

      assertNotNull(types);
      assertTrue(types.contains(Serializable.class));
   }

   @Test
   public void testParameterizedType() {
      ParameterizedTypeImpl listOfString = new ParameterizedTypeImpl(
            ArrayList.class, new Type[]{String.class}, null);
      HierarchyDiscovery discovery = new HierarchyDiscovery(listOfString);
      Set<Type> types = discovery.getTypeClosure();

      assertNotNull(types);
      assertFalse(types.isEmpty());
      // Should contain the parameterized type itself
      assertTrue(types.contains(listOfString));
   }

   @Test
   public void testGetResolvedTypeForSimpleClass() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(String.class);
      Type resolved = discovery.getResolvedType();
      // String has no type parameters, so resolved type is String itself
      assertTrue(resolved == String.class);
   }

   @Test
   public void testGetResolvedTypeForGenericClass() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(ArrayList.class);
      Type resolved = discovery.getResolvedType();
      // ArrayList<E> has type parameters, so should return ParameterizedType
      assertNotNull(resolved);
      assertTrue(resolved instanceof java.lang.reflect.ParameterizedType);
   }

   @Test
   public void testGetTypeClosureIdempotent() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(String.class);
      Set<Type> types1 = discovery.getTypeClosure();
      Set<Type> types2 = discovery.getTypeClosure();
      // Should return independent but equal sets
      assertEquals(types1, types2);
   }

   private void assertEquals(Set<Type> types1, Set<Type> types2) {
      assertTrue(types1.containsAll(types2));
      assertTrue(types2.containsAll(types1));
   }

   @Test
   public void testClassWithInterfaces() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(ArrayList.class);
      Set<Type> types = discovery.getTypeClosure();

      // ArrayList implements List, Collection, Iterable, etc.
      assertNotNull(types);
      assertTrue(types.size() > 3);
   }

   static class SimpleChild extends AbstractList<String> {
      @Override
      public String get(int index) {
         return null;
      }

      @Override
      public int size() {
         return 0;
      }
   }

   @Test
   public void testConcreteParameterizedSuperclass() {
      HierarchyDiscovery discovery = new HierarchyDiscovery(SimpleChild.class);
      Set<Type> types = discovery.getTypeClosure();

      assertNotNull(types);
      assertTrue(types.contains(SimpleChild.class));
      // Should discover the full hierarchy including List, Collection, etc.
      assertTrue(types.size() > 3);
   }
}
