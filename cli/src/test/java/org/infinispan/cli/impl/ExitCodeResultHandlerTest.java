package org.infinispan.cli.impl;

import static org.junit.Assert.assertEquals;

import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 16.2
 */
public class ExitCodeResultHandlerTest {

   private ExitCodeResultHandler handler;

   @Before
   public void setUp() {
      ExitCodeResultHandler.reset();
      handler = new ExitCodeResultHandler();
   }

   @Test
   public void testInitialExitCode() {
      assertEquals(0, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testOnSuccess() {
      ExitCodeResultHandler.fail(); // Set to 1 first
      assertEquals(1, ExitCodeResultHandler.exitCode());
      handler.onSuccess();
      assertEquals(0, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testOnFailure() {
      handler.onFailure(CommandResult.FAILURE);
      assertEquals(1, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testOnValidationFailure() {
      handler.onValidationFailure(CommandResult.FAILURE, new RuntimeException("test"));
      assertEquals(1, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testOnExecutionFailure() {
      handler.onExecutionFailure(CommandResult.FAILURE, new CommandException("test"));
      assertEquals(1, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testReset() {
      ExitCodeResultHandler.fail();
      assertEquals(1, ExitCodeResultHandler.exitCode());
      ExitCodeResultHandler.reset();
      assertEquals(0, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testFail() {
      ExitCodeResultHandler.fail();
      assertEquals(1, ExitCodeResultHandler.exitCode());
   }

   @Test
   public void testSuccessAfterFailure() {
      handler.onFailure(CommandResult.FAILURE);
      assertEquals(1, ExitCodeResultHandler.exitCode());
      handler.onSuccess();
      assertEquals(0, ExitCodeResultHandler.exitCode());
   }
}
