package org.infinispan.cli.printers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * @since 16.2
 */
public class DefaultRowPrinterTest {

   @Test
   public void testEvenWidthDistribution() {
      DefaultRowPrinter printer = new DefaultRowPrinter(81, 3);
      // Effective width = 81 - 3 + 1 = 79
      // Column width = 79 / 3 = 26 (for first two), last gets remainder = 27
      assertEquals(26, printer.columnWidth(0));
      assertEquals(26, printer.columnWidth(1));
      assertEquals(27, printer.columnWidth(2));
   }

   @Test
   public void testSingleColumn() {
      DefaultRowPrinter printer = new DefaultRowPrinter(100, 1);
      // Effective width = 100 - 1 + 1 = 100
      assertEquals(100, printer.columnWidth(0));
   }

   @Test
   public void testShowHeaderIsFalse() {
      DefaultRowPrinter printer = new DefaultRowPrinter(80, 2);
      assertFalse(printer.showHeader());
   }

   @Test
   public void testColumnHeaderIsEmpty() {
      DefaultRowPrinter printer = new DefaultRowPrinter(80, 2);
      assertEquals("", printer.columnHeader(0));
      assertEquals("", printer.columnHeader(1));
   }

   @Test
   public void testFormatColumnPassThrough() {
      DefaultRowPrinter printer = new DefaultRowPrinter(80, 2);
      assertEquals("test value", printer.formatColumn(0, "test value"));
      assertEquals("another", printer.formatColumn(1, "another"));
   }

   @Test
   public void testTwoColumns() {
      DefaultRowPrinter printer = new DefaultRowPrinter(41, 2);
      // Effective width = 41 - 2 + 1 = 40
      // Column width = 40 / 2 = 20 each
      assertEquals(20, printer.columnWidth(0));
      assertEquals(20, printer.columnWidth(1));
   }
}
