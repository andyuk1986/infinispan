package org.infinispan.reactive.internal.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

import io.reactivex.rxjava3.exceptions.CompositeException;

@Test(groups = "unit", testName = "reactive.internal.util.ExceptionHelperTest")
public class ExceptionHelperTest extends AbstractInfinispanTest {

   public void testWrapOrThrowRuntimeException() {
      RuntimeException re = new RuntimeException("test");
      RuntimeException result = ExceptionHelper.wrapOrThrow(re);
      assertEquals(re, result);
   }

   public void testWrapOrThrowCheckedException() {
      IOException checked = new IOException("test");
      RuntimeException result = ExceptionHelper.wrapOrThrow(checked);
      assertNotNull(result);
      assertEquals(checked, result.getCause());
   }

   public void testWrapOrThrowError() {
      Error error = new OutOfMemoryError("test");
      try {
         ExceptionHelper.wrapOrThrow(error);
         assert false : "Expected Error to be thrown";
      } catch (OutOfMemoryError expected) {
         assertEquals(error, expected);
      }
   }

   public void testAddThrowableFirstException() {
      AtomicReference<Throwable> field = new AtomicReference<>();
      RuntimeException ex = new RuntimeException("first");
      assertTrue(ExceptionHelper.addThrowable(field, ex));
      assertEquals(ex, field.get());
   }

   public void testAddThrowableSecondException() {
      AtomicReference<Throwable> field = new AtomicReference<>();
      RuntimeException first = new RuntimeException("first");
      RuntimeException second = new RuntimeException("second");
      ExceptionHelper.addThrowable(field, first);
      assertTrue(ExceptionHelper.addThrowable(field, second));
      assertTrue(field.get() instanceof CompositeException);
   }

   public void testAddThrowableTerminated() {
      AtomicReference<Throwable> field = new AtomicReference<>(ExceptionHelper.TERMINATED);
      RuntimeException ex = new RuntimeException("test");
      assertFalse(ExceptionHelper.addThrowable(field, ex));
   }

   public void testTerminate() {
      AtomicReference<Throwable> field = new AtomicReference<>();
      RuntimeException ex = new RuntimeException("test");
      field.set(ex);
      Throwable result = ExceptionHelper.terminate(field);
      assertEquals(ex, result);
      assertEquals(ExceptionHelper.TERMINATED, field.get());
   }

   public void testTerminateAlreadyTerminated() {
      AtomicReference<Throwable> field = new AtomicReference<>(ExceptionHelper.TERMINATED);
      Throwable result = ExceptionHelper.terminate(field);
      assertEquals(ExceptionHelper.TERMINATED, result);
   }

   public void testTerminateNull() {
      AtomicReference<Throwable> field = new AtomicReference<>();
      Throwable result = ExceptionHelper.terminate(field);
      assertNull(result);
      assertEquals(ExceptionHelper.TERMINATED, field.get());
   }

   public void testFlattenSimple() {
      RuntimeException ex = new RuntimeException("test");
      List<Throwable> result = ExceptionHelper.flatten(ex);
      assertEquals(1, result.size());
      assertEquals(ex, result.get(0));
   }

   public void testFlattenComposite() {
      RuntimeException first = new RuntimeException("first");
      RuntimeException second = new RuntimeException("second");
      CompositeException composite = new CompositeException(first, second);

      List<Throwable> result = ExceptionHelper.flatten(composite);
      assertEquals(2, result.size());
      assertEquals(first, result.get(0));
      assertEquals(second, result.get(1));
   }

   public void testFlattenNestedComposite() {
      RuntimeException a = new RuntimeException("a");
      RuntimeException b = new RuntimeException("b");
      RuntimeException c = new RuntimeException("c");
      CompositeException inner = new CompositeException(a, b);
      CompositeException outer = new CompositeException(inner, c);

      List<Throwable> result = ExceptionHelper.flatten(outer);
      assertEquals(3, result.size());
      assertEquals(a, result.get(0));
      assertEquals(b, result.get(1));
      assertEquals(c, result.get(2));
   }

   public void testThrowIfThrowableWithException() throws Exception {
      IOException ex = new IOException("test");
      Exception result = ExceptionHelper.throwIfThrowable(ex);
      assertEquals(ex, result);
   }

   public void testThrowIfThrowableWithError() {
      Error error = new OutOfMemoryError("test");
      try {
         ExceptionHelper.throwIfThrowable(error);
         assert false : "Expected Error to be thrown";
      } catch (Throwable expected) {
         assertEquals(error, expected);
      }
   }

   public void testTimeoutMessage() {
      String msg = ExceptionHelper.timeoutMessage(5, TimeUnit.SECONDS);
      assertNotNull(msg);
      assertTrue(msg.contains("5"));
      assertTrue(msg.contains("seconds"));
   }

   public void testNullWarning() {
      String msg = ExceptionHelper.nullWarning("test");
      assertTrue(msg.contains("test"));
      assertTrue(msg.contains("Null values"));
   }

   public void testCreateNullPointerException() {
      NullPointerException ex = ExceptionHelper.createNullPointerException("prefix");
      assertNotNull(ex);
      assertTrue(ex.getMessage().contains("prefix"));
   }

   public void testNullCheck() {
      String result = ExceptionHelper.nullCheck("value", "prefix");
      assertEquals("value", result);
   }

   public void testNullCheckWithNull() {
      try {
         ExceptionHelper.nullCheck(null, "prefix");
         assert false : "Expected NullPointerException";
      } catch (NullPointerException expected) {
         assertTrue(expected.getMessage().contains("prefix"));
      }
   }

   public void testTerminatedFillInStackTrace() {
      Throwable terminated = ExceptionHelper.TERMINATED;
      Throwable result = terminated.fillInStackTrace();
      assertEquals(terminated, result);
   }
}
