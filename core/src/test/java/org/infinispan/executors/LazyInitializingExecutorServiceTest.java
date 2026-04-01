package org.infinispan.executors;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.infinispan.factories.threads.EnhancedQueueExecutorFactory;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link LazyInitializingExecutorService}.
 */
@Test(groups = "unit", testName = "executors.LazyInitializingExecutorServiceTest")
public class LazyInitializingExecutorServiceTest extends AbstractInfinispanTest {

   private LazyInitializingExecutorService executor;

   @AfterMethod(alwaysRun = true)
   public void tearDown() {
      if (executor != null) {
         executor.shutdownNow();
         executor = null;
      }
   }

   private LazyInitializingExecutorService createExecutor() {
      EnhancedQueueExecutorFactory factory = EnhancedQueueExecutorFactory.create(2, 0);
      executor = new LazyInitializingExecutorService(factory,
            getTestThreadFactory("LazyExec"));
      return executor;
   }

   public void testIsShutdownBeforeInit() {
      LazyInitializingExecutorService exec = createExecutor();
      // Before any task submission, executor is null so isShutdown returns true
      assertTrue(exec.isShutdown());
   }

   public void testIsTerminatedBeforeInit() {
      LazyInitializingExecutorService exec = createExecutor();
      assertTrue(exec.isTerminated());
   }

   public void testAwaitTerminationBeforeInit() throws InterruptedException {
      LazyInitializingExecutorService exec = createExecutor();
      assertTrue(exec.awaitTermination(1, TimeUnit.MILLISECONDS));
   }

   public void testExecute() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      AtomicBoolean executed = new AtomicBoolean(false);
      exec.execute(() -> executed.set(true));
      // After execute, executor should be initialized
      assertFalse(exec.isShutdown());
      eventually(() -> executed.get(), 5000);
      assertTrue(executed.get());
   }

   public void testSubmitRunnable() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      AtomicBoolean executed = new AtomicBoolean(false);
      Future<?> f = exec.submit(() -> executed.set(true));
      f.get(5, TimeUnit.SECONDS);
      assertTrue(executed.get());
   }

   public void testSubmitRunnableWithResult() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Future<String> f = exec.submit(() -> {}, "result");
      assertEquals("result", f.get(5, TimeUnit.SECONDS));
   }

   public void testSubmitCallable() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Future<Integer> f = exec.submit(() -> 42);
      assertEquals(Integer.valueOf(42), f.get(5, TimeUnit.SECONDS));
   }

   public void testInvokeAll() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Callable<Integer> task1 = () -> 1;
      Callable<Integer> task2 = () -> 2;
      List<Future<Integer>> results = exec.invokeAll(Arrays.asList(task1, task2));
      assertEquals(2, results.size());
      assertEquals(Integer.valueOf(1), results.get(0).get(5, TimeUnit.SECONDS));
      assertEquals(Integer.valueOf(2), results.get(1).get(5, TimeUnit.SECONDS));
   }

   public void testInvokeAllWithTimeout() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Callable<Integer> task1 = () -> 1;
      List<Future<Integer>> results = exec.invokeAll(Arrays.asList(task1), 5, TimeUnit.SECONDS);
      assertEquals(1, results.size());
      assertEquals(Integer.valueOf(1), results.get(0).get(5, TimeUnit.SECONDS));
   }

   public void testInvokeAny() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Callable<Integer> task1 = () -> 1;
      Callable<Integer> task2 = () -> 2;
      Integer result = exec.invokeAny(Arrays.asList(task1, task2));
      assertNotNull(result);
      assertTrue(result == 1 || result == 2);
   }

   public void testInvokeAnyWithTimeout() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      Callable<Integer> task1 = () -> 1;
      Integer result = exec.invokeAny(Arrays.asList(task1), 5, TimeUnit.SECONDS);
      assertEquals(Integer.valueOf(1), result);
   }

   public void testShutdown() {
      LazyInitializingExecutorService exec = createExecutor();
      // Force initialization
      exec.execute(() -> {});
      exec.shutdown();
      assertTrue(exec.isShutdown());
   }

   public void testShutdownBeforeInit() {
      LazyInitializingExecutorService exec = createExecutor();
      exec.shutdown();
      assertTrue(exec.isShutdown());
   }

   public void testShutdownNow() {
      LazyInitializingExecutorService exec = createExecutor();
      exec.execute(() -> {});
      List<Runnable> remaining = exec.shutdownNow();
      assertNotNull(remaining);
      assertTrue(exec.isShutdown());
   }

   public void testShutdownNowBeforeInit() {
      LazyInitializingExecutorService exec = createExecutor();
      List<Runnable> remaining = exec.shutdownNow();
      assertNotNull(remaining);
      assertTrue(exec.isShutdown());
   }

   public void testAwaitTerminationAfterShutdown() throws Exception {
      LazyInitializingExecutorService exec = createExecutor();
      exec.execute(() -> {});
      exec.shutdown();
      assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS));
      assertTrue(exec.isTerminated());
   }
}
