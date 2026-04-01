package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @since 16.2
 */
public class ReflectionsTest {

   // --- Test classes for hierarchy discovery ---
   static class Parent {
      protected String parentField;
      protected void parentMethod() {}
   }

   static class Child extends Parent {
      private int childField;
      public void childMethod() {}
      private void privateMethod() { }
   }

   // --- cast ---
   @Test
   public void testCast() {
      Object str = "hello";
      String result = Reflections.cast(str);
      assertEquals("hello", result);
   }

   // --- getAllDeclaredMethods ---
   @Test
   public void testGetAllDeclaredMethods() {
      Set<Method> methods = Reflections.getAllDeclaredMethods(Child.class);
      assertNotNull(methods);
      assertTrue(methods.size() >= 3); // childMethod, privateMethod, parentMethod
      assertTrue(methods.stream().anyMatch(m -> m.getName().equals("childMethod")));
      assertTrue(methods.stream().anyMatch(m -> m.getName().equals("parentMethod")));
      assertTrue(methods.stream().anyMatch(m -> m.getName().equals("privateMethod")));
   }

   @Test
   public void testGetAllDeclaredMethodsExcludesObject() {
      Set<Method> methods = Reflections.getAllDeclaredMethods(Child.class);
      // Should not include Object methods like toString, equals, etc.
      assertFalse(methods.stream().anyMatch(m -> m.getName().equals("wait")));
   }

   // --- getAllDeclaredFields ---
   @Test
   public void testGetAllDeclaredFields() {
      Set<Field> fields = Reflections.getAllDeclaredFields(Child.class);
      assertNotNull(fields);
      assertTrue(fields.size() >= 2); // childField, parentField
      assertTrue(fields.stream().anyMatch(f -> f.getName().equals("childField")));
      assertTrue(fields.stream().anyMatch(f -> f.getName().equals("parentField")));
   }

   // --- invokeMethod ---
   @Test
   public void testInvokeMethod() throws NoSuchMethodException {
      Method method = String.class.getMethod("length");
      Object result = Reflections.invokeMethod(true, method, "hello");
      assertEquals(5, result);
   }

   @Test
   public void testInvokeMethodWithReturnType() throws NoSuchMethodException {
      Method method = String.class.getMethod("length");
      Integer result = Reflections.invokeMethod(true, method, Integer.class, "hello");
      assertEquals(Integer.valueOf(5), result);
   }

   @Test(expected = RuntimeException.class)
   public void testInvokeMethodWithIllegalAccess() throws NoSuchMethodException {
      Method method = Child.class.getDeclaredMethod("privateMethod");
      // Don't set accessible, should fail
      Reflections.invokeMethod(false, method, new Child());
   }

   // --- getRawType ---
   @Test
   public void testGetRawTypeFromClass() {
      Class<?> raw = Reflections.getRawType(String.class);
      assertEquals(String.class, raw);
   }

   @Test
   public void testGetRawTypeFromParameterizedType() {
      ParameterizedTypeImpl pt = new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null);
      Class<?> raw = Reflections.getRawType(pt);
      assertEquals(List.class, raw);
   }

   @Test
   public void testGetRawTypeFromUnknownType() {
      // TypeVariable or WildcardType should return null
      assertNull(Reflections.getRawType(new Type() {
         @Override
         public String getTypeName() {
            return "unknown";
         }
      }));
   }

   // --- isSerializable ---
   @Test
   public void testIsSerializablePrimitive() {
      assertTrue(Reflections.isSerializable(int.class));
      assertTrue(Reflections.isSerializable(boolean.class));
   }

   @Test
   public void testIsSerializableString() {
      assertTrue(Reflections.isSerializable(String.class));
   }

   @Test
   public void testIsSerializableNonSerializable() {
      assertFalse(Reflections.isSerializable(Object.class));
   }

   // --- isAssignableFrom (Class, Type[], Class, Type[]) ---
   @Test
   public void testIsAssignableFromRawTypes() {
      assertTrue(Reflections.isAssignableFrom(Object.class, Reflections.EMPTY_TYPES, String.class, Reflections.EMPTY_TYPES));
      assertFalse(Reflections.isAssignableFrom(String.class, Reflections.EMPTY_TYPES, Object.class, Reflections.EMPTY_TYPES));
   }

   @Test
   public void testIsAssignableFromPrimitiveBoxing() {
      // int.class should be assignable from Integer.class via boxing
      assertTrue(Reflections.isAssignableFrom(int.class, Reflections.EMPTY_TYPES, Integer.class, Reflections.EMPTY_TYPES));
   }

   // --- isAssignableFrom (Type, Type) ---
   @Test
   public void testIsAssignableFromTypes() {
      assertTrue(Reflections.isAssignableFrom((Type) Object.class, (Type) String.class));
      assertFalse(Reflections.isAssignableFrom((Type) String.class, (Type) Integer.class));
   }

   @Test
   public void testIsAssignableFromParameterizedTypes() {
      ParameterizedTypeImpl listOfString = new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null);
      ParameterizedTypeImpl listOfObject = new ParameterizedTypeImpl(List.class, new Type[]{Object.class}, null);

      // List<Object> is NOT assignable from List<String> (invariant generics)
      assertFalse(Reflections.isAssignableFrom(listOfString, listOfObject));
   }

   // --- matches ---
   @Test
   public void testMatchesExactTypes() {
      assertTrue(Reflections.matches((Type) String.class, (Type) String.class));
      assertFalse(Reflections.matches((Type) String.class, (Type) Integer.class));
   }

   @Test
   public void testMatchesTypeInSet() {
      Set<Type> types = Set.of(String.class, Integer.class);
      assertTrue(Reflections.matches((Type) String.class, types));
      assertFalse(Reflections.matches((Type) Double.class, types));
   }

   @Test
   public void testMatchesSetsOfTypes() {
      Set<Type> set1 = Set.of(String.class, Integer.class);
      Set<Type> set2 = Set.of(Double.class, String.class);
      assertTrue(Reflections.matches(set1, set2)); // String is common
   }

   @Test
   public void testMatchesSetsNoOverlap() {
      Set<Type> set1 = Set.of(String.class);
      Set<Type> set2 = Set.of(Double.class);
      assertFalse(Reflections.matches(set1, set2));
   }

   // --- isTypeBounded ---
   @Test
   public void testIsTypeBoundedNoConstraints() {
      assertTrue(Reflections.isTypeBounded(String.class, Reflections.EMPTY_TYPES, Reflections.EMPTY_TYPES));
   }

   @Test
   public void testIsTypeBoundedWithUpperBound() {
      Type[] upperBounds = new Type[]{Serializable.class};
      assertTrue(Reflections.isTypeBounded(String.class, Reflections.EMPTY_TYPES, upperBounds));
      assertFalse(Reflections.isTypeBounded(Object.class, Reflections.EMPTY_TYPES, new Type[]{String.class}));
   }

   // --- isAssignableFrom (Type[], Type) ---
   @Test
   public void testIsAssignableFromTypesArray() {
      Type[] types = new Type[]{Object.class, Serializable.class};
      assertTrue(Reflections.isAssignableFrom(types, String.class));
   }

   // --- isAssignableFrom (Type, Type[]) ---
   @Test
   public void testIsAssignableFromTypeToArray() {
      Type[] types = new Type[]{String.class, Integer.class};
      assertTrue(Reflections.isAssignableFrom((Type) Object.class, types));
   }

   // --- matches (Class, Type[], Type) ---
   @Test
   public void testMatchesClassWithParameterizedType() {
      ParameterizedTypeImpl listOfString = new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null);
      assertTrue(Reflections.matches(List.class, new Type[]{String.class}, listOfString));
      assertFalse(Reflections.matches(Set.class, new Type[]{String.class}, listOfString));
   }

   @Test
   public void testMatchesClassWithPlainClass() {
      assertTrue(Reflections.matches(String.class, Reflections.EMPTY_TYPES, String.class));
      assertFalse(Reflections.matches(String.class, Reflections.EMPTY_TYPES, Integer.class));
   }

   // --- isAssignableFrom (Class, Type[], Type) ---
   @Test
   public void testIsAssignableFromClassToParameterized() {
      ParameterizedTypeImpl listOfString = new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null);
      assertTrue(Reflections.isAssignableFrom(List.class, new Type[]{String.class}, listOfString));
   }

   @Test
   public void testIsAssignableFromClassToClass() {
      assertTrue(Reflections.isAssignableFrom(Object.class, Reflections.EMPTY_TYPES, (Type) String.class));
   }

   // --- Empty arrays ---
   @Test
   public void testEmptyArrayConstants() {
      assertEquals(0, Reflections.EMPTY_ANNOTATION_ARRAY.length);
      assertEquals(0, Reflections.EMPTY_OBJECT_ARRAY.length);
      assertEquals(0, Reflections.EMPTY_TYPES.length);
   }
}
