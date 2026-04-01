package org.infinispan.cli.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import org.aesh.command.converter.ConverterInvocation;
import org.aesh.console.AeshContext;
import org.junit.Test;

/**
 * @since 16.2
 */
public class LocalDateTimeConverterTest {

   @Test
   public void testConvertValidDate() {
      LocalDateTimeConverter converter = new LocalDateTimeConverter();
      LocalDateTime result = converter.convert(new SimpleConverterInvocation("04/Sep/2024:13:18:14"));
      assertEquals(2024, result.getYear());
      assertEquals(9, result.getMonthValue());
      assertEquals(4, result.getDayOfMonth());
      assertEquals(13, result.getHour());
      assertEquals(18, result.getMinute());
      assertEquals(14, result.getSecond());
   }

   @Test
   public void testConvertNull() {
      LocalDateTimeConverter converter = new LocalDateTimeConverter();
      LocalDateTime result = converter.convert(new SimpleConverterInvocation(null));
      assertNull(result);
   }

   @Test
   public void testConvertEmpty() {
      LocalDateTimeConverter converter = new LocalDateTimeConverter();
      LocalDateTime result = converter.convert(new SimpleConverterInvocation(""));
      assertNull(result);
   }

   @Test(expected = Exception.class)
   public void testConvertInvalidFormat() {
      LocalDateTimeConverter converter = new LocalDateTimeConverter();
      converter.convert(new SimpleConverterInvocation("2024-09-04 13:18:14"));
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
