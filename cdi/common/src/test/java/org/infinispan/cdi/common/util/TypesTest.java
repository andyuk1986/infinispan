package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @since 16.2
 */
public class TypesTest {

   @Test
   public void testBoxedBoolean() {
      assertEquals(Boolean.class, Types.boxedClass(boolean.class));
   }

   @Test
   public void testBoxedCharacter() {
      assertEquals(Character.class, Types.boxedClass(char.class));
   }

   @Test
   public void testBoxedByte() {
      assertEquals(Byte.class, Types.boxedClass(byte.class));
   }

   @Test
   public void testBoxedShort() {
      assertEquals(Short.class, Types.boxedClass(short.class));
   }

   @Test
   public void testBoxedInteger() {
      assertEquals(Integer.class, Types.boxedClass(int.class));
   }

   @Test
   public void testBoxedLong() {
      assertEquals(Long.class, Types.boxedClass(long.class));
   }

   @Test
   public void testBoxedFloat() {
      assertEquals(Float.class, Types.boxedClass(float.class));
   }

   @Test
   public void testBoxedDouble() {
      assertEquals(Double.class, Types.boxedClass(double.class));
   }

   @Test
   public void testBoxedVoid() {
      assertEquals(Void.class, Types.boxedClass(void.class));
   }

   @Test
   public void testNonPrimitivePassThrough() {
      assertEquals(String.class, Types.boxedClass(String.class));
      assertEquals(Integer.class, Types.boxedClass(Integer.class));
      assertEquals(Object.class, Types.boxedClass(Object.class));
   }
}
