package org.infinispan.cli.printers;

import static org.junit.Assert.assertTrue;

import org.infinispan.cli.AeshTestShell;
import org.junit.Test;

/**
 * @since 16.2
 */
public class PrettyPrinterFactoryTest {

   @Test
   public void testForModeTable() {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 2);
      PrettyPrinter printer = PrettyPrinter.forMode(PrettyPrinter.PrettyPrintMode.TABLE, shell, rowPrinter);
      assertTrue(printer instanceof TablePrettyPrinter);
   }

   @Test
   public void testForModeJson() {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 2);
      PrettyPrinter printer = PrettyPrinter.forMode(PrettyPrinter.PrettyPrintMode.JSON, shell, rowPrinter);
      assertTrue(printer instanceof JsonPrettyPrinter);
   }

   @Test
   public void testForModeCsv() {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 2);
      PrettyPrinter printer = PrettyPrinter.forMode(PrettyPrinter.PrettyPrintMode.CSV, shell, rowPrinter);
      assertTrue(printer instanceof CsvPrettyPrinter);
   }
}
