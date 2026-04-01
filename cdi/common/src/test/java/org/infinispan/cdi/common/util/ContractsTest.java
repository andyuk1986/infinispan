package org.infinispan.cdi.common.util;

import org.junit.Test;

/**
 * @since 16.2
 */
public class ContractsTest {

   @Test
   public void testAssertNotNullPasses() {
      Contracts.assertNotNull("value", "should not fail");
   }

   @Test(expected = NullPointerException.class)
   public void testAssertNotNullThrows() {
      Contracts.assertNotNull(null, "param is null");
   }

   @Test
   public void testAssertNotNullExceptionMessage() {
      try {
         Contracts.assertNotNull(null, "custom message");
      } catch (NullPointerException e) {
         org.junit.Assert.assertEquals("custom message", e.getMessage());
      }
   }
}
