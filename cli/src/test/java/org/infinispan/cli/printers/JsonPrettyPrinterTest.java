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
public class JsonPrettyPrinterTest {

   @Test
   public void testSingleSimpleItem() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      JsonPrettyPrinter printer = new JsonPrettyPrinter(shell);

      printer.printItem(Map.of("", "value1"));
      printer.close();

      String output = shell.getBuffer();
      assertTrue(output.contains("["));
      assertTrue(output.contains("]"));
      assertTrue(output.contains("\"value1\""));
   }

   @Test
   public void testMultipleSimpleItems() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      JsonPrettyPrinter printer = new JsonPrettyPrinter(shell);

      printer.printItem(Map.of("", "value1"));
      printer.printItem(Map.of("", "value2"));
      printer.close();

      String output = shell.getBuffer();
      assertTrue(output.contains("\"value1\""));
      assertTrue(output.contains("\"value2\""));
      assertTrue(output.contains(","));
   }

   @Test
   public void testComplexItem() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      JsonPrettyPrinter printer = new JsonPrettyPrinter(shell);

      Map<String, String> item = new LinkedHashMap<>();
      item.put("name", "John");
      item.put("age", "30");
      printer.printItem(item);
      printer.close();

      String output = shell.getBuffer();
      assertTrue(output.contains("{"));
      assertTrue(output.contains("}"));
      assertTrue(output.contains("name"));
      assertTrue(output.contains("\"John\""));
      assertTrue(output.contains("age"));
      assertTrue(output.contains("\"30\""));
   }

   @Test
   public void testEmptyOutput() throws IOException {
      AeshTestShell shell = new AeshTestShell();
      JsonPrettyPrinter printer = new JsonPrettyPrinter(shell);
      printer.close();

      String output = shell.getBuffer();
      assertTrue(output.contains("["));
      assertTrue(output.contains("]"));
   }
}
