package org.infinispan.cli.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.aesh.command.converter.ConverterInvocation;
import org.aesh.command.validator.OptionValidatorException;
import org.aesh.console.AeshContext;
import org.junit.Test;

/**
 * @since 16.2
 */
public class EnumConverterTest {

   enum TestEnum {
      ALPHA, BETA, GAMMA_DELTA
   }

   static class TestEnumConverter extends EnumConverter<TestEnum> {
      TestEnumConverter() {
         super(TestEnum.class);
      }
   }

   @Test
   public void testConvertLowerCase() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      assertEquals(TestEnum.ALPHA, converter.convert(ci("alpha")));
      assertEquals(TestEnum.BETA, converter.convert(ci("beta")));
   }

   @Test
   public void testConvertUpperCase() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      assertEquals(TestEnum.ALPHA, converter.convert(ci("ALPHA")));
   }

   @Test
   public void testConvertMixedCase() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      assertEquals(TestEnum.ALPHA, converter.convert(ci("Alpha")));
   }

   @Test
   public void testConvertWithDashToUnderscore() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      // Dashes should be replaced with underscores
      assertEquals(TestEnum.GAMMA_DELTA, converter.convert(ci("gamma-delta")));
   }

   @Test
   public void testNullInput() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      assertNull(converter.convert(ci(null)));
   }

   @Test
   public void testEmptyInput() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      assertNull(converter.convert(ci("")));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testInvalidValue() throws OptionValidatorException {
      TestEnumConverter converter = new TestEnumConverter();
      converter.convert(ci("nonexistent"));
   }

   @Test
   public void testEncryptionConverterNullHandling() throws OptionValidatorException {
      EncryptionConverter converter = new EncryptionConverter();
      assertNull(converter.convert(ci(null)));
      assertNull(converter.convert(ci("")));
   }

   @Test
   public void testExposeConverterNullHandling() throws OptionValidatorException {
      ExposeConverter converter = new ExposeConverter();
      assertNull(converter.convert(ci(null)));
      assertNull(converter.convert(ci("")));
   }

   private static ConverterInvocation ci(String input) {
      return new ConverterInvocation() {
         @Override
         public String getInput() {
            return input;
         }

         @Override
         public AeshContext getAeshContext() {
            return null;
         }
      };
   }
}
