package org.infinispan.cli.printers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 16.2
 */
public class CacheEntryRowPrinterTest {

   @Test
   public void testSingleColumn() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(100, 1);
      assertEquals(100, printer.columnWidth(0));
   }

   @Test
   public void testTwoColumnsWideEnough() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(100, 2);
      int keyWidth = printer.columnWidth(0);
      int valueWidth = printer.columnWidth(1);
      assertTrue(keyWidth > 0);
      assertTrue(valueWidth > 0);
      // Key width should be min(100/3, 15)
      assertEquals(15, keyWidth);
      // Value width = 100 - 15 - 1 = 84
      assertEquals(84, valueWidth);
   }

   @Test
   public void testTwoColumnsNarrow() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(15, 2);
      assertEquals(6, printer.columnWidth(0));
      assertEquals(13, printer.columnWidth(1));
   }

   @Test
   public void testSevenColumns() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      assertEquals(6, printer.columnWidth(2)); // TTL
      assertEquals(6, printer.columnWidth(3)); // Idle
      assertEquals(19, printer.columnWidth(4)); // Created
      assertEquals(19, printer.columnWidth(5)); // LastUsed
      assertEquals(19, printer.columnWidth(6)); // Expires
   }

   @Test
   public void testSevenColumnsNarrow() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(75, 7);
      assertEquals(6, printer.columnWidth(0));
      assertEquals(13, printer.columnWidth(1));
   }

   @Test
   public void testShowHeader() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(100, 2);
      assertTrue(printer.showHeader());
   }

   @Test
   public void testColumnHeaders() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      assertEquals("Key", printer.columnHeader(0));
      assertEquals("Value", printer.columnHeader(1));
      assertEquals("TTL", printer.columnHeader(2));
      assertEquals("Idle", printer.columnHeader(3));
      assertEquals("Created", printer.columnHeader(4));
      assertEquals("LastUsed", printer.columnHeader(5));
      assertEquals("Expires", printer.columnHeader(6));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testInvalidColumnHeader() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      printer.columnHeader(7);
   }

   @Test
   public void testFormatColumnKeyValue() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      assertEquals("myKey", printer.formatColumn(0, "myKey"));
      assertEquals("myValue", printer.formatColumn(1, "myValue"));
   }

   @Test
   public void testFormatColumnImmortalEntry() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      // Negative values indicate immortal entries
      assertEquals("\u221E", printer.formatColumn(2, "-1"));
      assertEquals("\u221E", printer.formatColumn(3, "-1"));
   }

   @Test
   public void testFormatColumnDuration() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      // TTL column (index 2): 3600 seconds = 1 hour
      String formatted = printer.formatColumn(2, "3600");
      assertEquals("1h", formatted);
   }

   @Test
   public void testFormatColumnDateTime() {
      CacheEntryRowPrinter printer = new CacheEntryRowPrinter(120, 7);
      // Created column (index 4): should format as date
      String formatted = printer.formatColumn(4, "1655871119343");
      assertTrue(formatted.contains("2022"));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testInvalidColumnCount() {
      new CacheEntryRowPrinter(100, 3);
   }
}
