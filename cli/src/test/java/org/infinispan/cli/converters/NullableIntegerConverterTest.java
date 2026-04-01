package org.infinispan.cli.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.aesh.command.converter.ConverterInvocation;
import org.aesh.console.AeshContext;
import org.junit.Test;

/**
 * @since 16.2
 */
public class NullableIntegerConverterTest {

   @Test
   public void testConvertValidInteger() {
      NullableIntegerConverter converter = new NullableIntegerConverter();
      Integer result = converter.convert(new SimpleConverterInvocation("42"));
      assertEquals(Integer.valueOf(42), result);
   }

   @Test
   public void testConvertNull() {
      NullableIntegerConverter converter = new NullableIntegerConverter();
      Integer result = converter.convert(new SimpleConverterInvocation(null));
      assertNull(result);
   }

   @Test
   public void testConvertEmpty() {
      NullableIntegerConverter converter = new NullableIntegerConverter();
      Integer result = converter.convert(new SimpleConverterInvocation(""));
      assertNull(result);
   }

   @Test
   public void testConvertNegative() {
      NullableIntegerConverter converter = new NullableIntegerConverter();
      Integer result = converter.convert(new SimpleConverterInvocation("-5"));
      assertEquals(Integer.valueOf(-5), result);
   }

   @Test(expected = NumberFormatException.class)
   public void testConvertInvalidInput() {
      NullableIntegerConverter converter = new NullableIntegerConverter();
      converter.convert(new SimpleConverterInvocation("not_a_number"));
   }

   private static class SimpleConverterInvocation implements ConverterInvocation {
      private final String input;

      SimpleConverterInvocation(String input) {
         this.input = input;
      }

      @Override
      public String getInput() {
         return input;
      }

      @Override
      public AeshContext getAeshContext() {
         return null;
      }
   }
}
