package org.infinispan.reactive.internal.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.infinispan.test.AbstractInfinispanTest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "reactive.internal.util.AtomicThrowableTest")
public class AtomicThrowableTest extends AbstractInfinispanTest {

   public void testTryAddThrowable() {
      AtomicThrowable at = new AtomicThrowable();
      RuntimeException ex = new RuntimeException("test");
      assertTrue(at.tryAddThrowable(ex));
      assertEquals(ex, at.get());
   }

   public void testTryAddThrowableAfterTerminate() {
      AtomicThrowable at = new AtomicThrowable();
      at.terminate();
      assertFalse(at.tryAddThrowable(new RuntimeException("test")));
   }

   public void testTryAddThrowableOrReportSuccess() {
      AtomicThrowable at = new AtomicThrowable();
      assertTrue(at.tryAddThrowableOrReport(new RuntimeException("test")));
   }

   public void testTryAddThrowableOrReportTerminated() {
      AtomicThrowable at = new AtomicThrowable();
      at.terminate();
      // Should return false and report to RxJavaPlugins
      assertFalse(at.tryAddThrowableOrReport(new RuntimeException("test")));
   }

   public void testTerminate() {
      AtomicThrowable at = new AtomicThrowable();
      RuntimeException ex = new RuntimeException("test");
      at.set(ex);
      Throwable result = at.terminate();
      assertEquals(ex, result);
      assertTrue(at.isTerminated());
   }

   public void testTerminateEmpty() {
      AtomicThrowable at = new AtomicThrowable();
      Throwable result = at.terminate();
      assertNull(result);
      assertTrue(at.isTerminated());
   }

   public void testIsTerminated() {
      AtomicThrowable at = new AtomicThrowable();
      assertFalse(at.isTerminated());
      at.terminate();
      assertTrue(at.isTerminated());
   }

   public void testTryTerminateAndReportNoError() {
      AtomicThrowable at = new AtomicThrowable();
      at.tryTerminateAndReport();
      assertTrue(at.isTerminated());
   }

   public void testTryTerminateAndReportWithError() {
      AtomicThrowable at = new AtomicThrowable();
      at.tryAddThrowable(new RuntimeException("test"));
      // This will report to RxJavaPlugins
      at.tryTerminateAndReport();
      assertTrue(at.isTerminated());
   }

   public void testTryTerminateAndReportAlreadyTerminated() {
      AtomicThrowable at = new AtomicThrowable();
      at.terminate();
      at.tryTerminateAndReport();
      assertTrue(at.isTerminated());
   }

   public void testTryTerminateConsumerSubscriberComplete() {
      AtomicThrowable at = new AtomicThrowable();
      AtomicBoolean completed = new AtomicBoolean(false);
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Subscriber<Object> subscriber = new Subscriber<>() {
         @Override
         public void onSubscribe(Subscription s) { }

         @Override
         public void onNext(Object o) { }

         @Override
         public void onError(Throwable t) {
            errorRef.set(t);
         }

         @Override
         public void onComplete() {
            completed.set(true);
         }
      };

      at.tryTerminateConsumer(subscriber);
      assertTrue(completed.get());
      assertNull(errorRef.get());
   }

   public void testTryTerminateConsumerSubscriberError() {
      AtomicThrowable at = new AtomicThrowable();
      RuntimeException ex = new RuntimeException("test");
      at.tryAddThrowable(ex);

      AtomicBoolean completed = new AtomicBoolean(false);
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Subscriber<Object> subscriber = new Subscriber<>() {
         @Override
         public void onSubscribe(Subscription s) { }

         @Override
         public void onNext(Object o) { }

         @Override
         public void onError(Throwable t) {
            errorRef.set(t);
         }

         @Override
         public void onComplete() {
            completed.set(true);
         }
      };

      at.tryTerminateConsumer(subscriber);
      assertFalse(completed.get());
      assertNotNull(errorRef.get());
   }

   public void testTryTerminateConsumerSubscriberAlreadyTerminated() {
      AtomicThrowable at = new AtomicThrowable();
      at.terminate(); // set to TERMINATED

      AtomicBoolean completed = new AtomicBoolean(false);
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Subscriber<Object> subscriber = new Subscriber<>() {
         @Override
         public void onSubscribe(Subscription s) { }

         @Override
         public void onNext(Object o) { }

         @Override
         public void onError(Throwable t) {
            errorRef.set(t);
         }

         @Override
         public void onComplete() {
            completed.set(true);
         }
      };

      at.tryTerminateConsumer(subscriber);
      // Should not signal anything since it was already terminated
      assertFalse(completed.get());
      assertNull(errorRef.get());
   }
}
