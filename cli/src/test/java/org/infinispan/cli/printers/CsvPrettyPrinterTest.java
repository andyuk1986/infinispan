package org.infinispan.cli.printers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.infinispan.cli.AeshTestShell;
import org.junit.Test;

/**
 * @since 16.2
 */
public class CsvPrettyPrinterTest {

   @Test
   public void testSingleRow() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 2);
      CsvPrettyPrinter printer = new CsvPrettyPrinter(shell, rowPrinter);

      Map<String, String> item = new LinkedHashMap<>();
      item.put("col1", "value1");
      item.put("col2", "value2");
      printer.printItem(item);
      printer.close();

      String output = shell.getBuffer();
      // Check header is present
      assertTrue(output.contains("# "));
      // Check values are quoted
      assertTrue(output.contains("\"value1\""));
      assertTrue(output.contains("\"value2\""));
   }

   @Test
   public void testMultipleRows() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 2);
      CsvPrettyPrinter printer = new CsvPrettyPrinter(shell, rowPrinter);

      Map<String, String> item1 = new LinkedHashMap<>();
      item1.put("col1", "a");
      item1.put("col2", "b");
      printer.printItem(item1);

      Map<String, String> item2 = new LinkedHashMap<>();
      item2.put("col1", "c");
      item2.put("col2", "d");
      printer.printItem(item2);
      printer.close();

      String output = shell.getBuffer();
      // Header should only appear once
      long headerCount = output.lines().filter(l -> l.startsWith("# ")).count();
      assertTrue(headerCount == 1);
      assertTrue(output.contains("\"a\""));
      assertTrue(output.contains("\"d\""));
   }

   @Test
   public void testValueWithQuotes() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      DefaultRowPrinter rowPrinter = new DefaultRowPrinter(80, 1);
      CsvPrettyPrinter printer = new CsvPrettyPrinter(shell, rowPrinter);

      Map<String, String> item = new LinkedHashMap<>();
      item.put("col1", "value with \"quotes\"");
      printer.printItem(item);
      printer.close();

      String output = shell.getBuffer();
      // Quotes should be escaped
      assertTrue(output.contains("\\\"quotes\\\""));
   }
}
