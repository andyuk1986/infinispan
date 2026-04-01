package org.infinispan.reactive.internal.subscriptions;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.infinispan.test.AbstractInfinispanTest;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "reactive.internal.subscriptions.SubscriptionHelperTest")
public class SubscriptionHelperTest extends AbstractInfinispanTest {

   private Subscription mockSubscription() {
      return new Subscription() {
         long requested = 0;
         boolean cancelled = false;

         @Override
         public void request(long n) {
            requested += n;
         }

         @Override
         public void cancel() {
            cancelled = true;
         }
      };
   }

   public void testValidateSubscriptionNullNext() {
      assertFalse(SubscriptionHelper.validate(null, null));
   }

   public void testValidateSubscriptionCurrentNotNull() {
      Subscription current = mockSubscription();
      Subscription next = mockSubscription();
      assertFalse(SubscriptionHelper.validate(current, next));
   }

   public void testValidateSubscriptionValid() {
      assertTrue(SubscriptionHelper.validate(null, mockSubscription()));
   }

   public void testValidateRequestPositive() {
      assertTrue(SubscriptionHelper.validate(1));
      assertTrue(SubscriptionHelper.validate(Long.MAX_VALUE));
   }

   public void testValidateRequestNonPositive() {
      assertFalse(SubscriptionHelper.validate(0));
      assertFalse(SubscriptionHelper.validate(-1));
   }

   public void testSetNormal() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      Subscription s = mockSubscription();
      assertTrue(SubscriptionHelper.set(field, s));
      assertEquals(s, field.get());
   }

   public void testSetCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.set(field, mockSubscription()));
   }

   public void testSetNullOnCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.set(field, null));
   }

   public void testSetReplacesOld() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      Subscription first = mockSubscription();
      SubscriptionHelper.set(field, first);
      Subscription second = mockSubscription();
      assertTrue(SubscriptionHelper.set(field, second));
      assertEquals(second, field.get());
   }

   public void testSetOnceSuccess() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      Subscription s = mockSubscription();
      assertTrue(SubscriptionHelper.setOnce(field, s));
   }

   public void testSetOnceAlreadySet() {
      AtomicReference<Subscription> field = new AtomicReference<>(mockSubscription());
      assertFalse(SubscriptionHelper.setOnce(field, mockSubscription()));
   }

   public void testSetOnceOnCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.setOnce(field, mockSubscription()));
   }

   public void testReplaceNormal() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      assertTrue(SubscriptionHelper.replace(field, mockSubscription()));
   }

   public void testReplaceOnCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.replace(field, mockSubscription()));
   }

   public void testReplaceNullOnCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.replace(field, null));
   }

   public void testCancelNormal() {
      AtomicReference<Subscription> field = new AtomicReference<>(mockSubscription());
      assertTrue(SubscriptionHelper.cancel(field));
      assertEquals(SubscriptionHelper.CANCELLED, field.get());
   }

   public void testCancelNull() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      assertTrue(SubscriptionHelper.cancel(field));
      assertEquals(SubscriptionHelper.CANCELLED, field.get());
   }

   public void testCancelAlreadyCancelled() {
      AtomicReference<Subscription> field = new AtomicReference<>(SubscriptionHelper.CANCELLED);
      assertFalse(SubscriptionHelper.cancel(field));
   }

   public void testDeferredSetOnce() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      AtomicLong requested = new AtomicLong(5);
      Subscription s = mockSubscription();
      assertTrue(SubscriptionHelper.deferredSetOnce(field, requested, s));
      assertEquals(0L, requested.get());
   }

   public void testDeferredSetOnceNoRequest() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      AtomicLong requested = new AtomicLong(0);
      Subscription s = mockSubscription();
      assertTrue(SubscriptionHelper.deferredSetOnce(field, requested, s));
   }

   public void testDeferredSetOnceAlreadySet() {
      AtomicReference<Subscription> field = new AtomicReference<>(mockSubscription());
      AtomicLong requested = new AtomicLong(5);
      assertFalse(SubscriptionHelper.deferredSetOnce(field, requested, mockSubscription()));
   }

   public void testDeferredRequest() {
      AtomicReference<Subscription> field = new AtomicReference<>(mockSubscription());
      AtomicLong requested = new AtomicLong(0);
      SubscriptionHelper.deferredRequest(field, requested, 5);
      // request should be forwarded to the subscription directly
   }

   public void testDeferredRequestNoSubscription() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      AtomicLong requested = new AtomicLong(0);
      SubscriptionHelper.deferredRequest(field, requested, 5);
      assertEquals(5L, requested.get());
   }

   public void testDeferredRequestInvalidAmount() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      AtomicLong requested = new AtomicLong(0);
      SubscriptionHelper.deferredRequest(field, requested, -1);
      assertEquals(0L, requested.get());
   }

   public void testSetOnceWithRequest() {
      AtomicReference<Subscription> field = new AtomicReference<>();
      Subscription s = mockSubscription();
      assertTrue(SubscriptionHelper.setOnce(field, s, 10));
   }

   public void testSetOnceWithRequestAlreadySet() {
      AtomicReference<Subscription> field = new AtomicReference<>(mockSubscription());
      assertFalse(SubscriptionHelper.setOnce(field, mockSubscription(), 10));
   }

   public void testCancelledRequestIsNoop() {
      SubscriptionHelper.CANCELLED.request(10);
   }

   public void testCancelledCancelIsNoop() {
      SubscriptionHelper.CANCELLED.cancel();
   }

   // ---- AsyncSubscription ----

   public void testAsyncSubscriptionDispose() {
      AsyncSubscription as = new AsyncSubscription();
      assertFalse(as.isDisposed());
      as.dispose();
      assertTrue(as.isDisposed());
   }

   public void testAsyncSubscriptionCancel() {
      AsyncSubscription as = new AsyncSubscription();
      as.cancel();
      assertTrue(as.isDisposed());
   }

   public void testAsyncSubscriptionRequest() {
      AsyncSubscription as = new AsyncSubscription();
      as.request(10); // Should not throw
   }

   public void testAsyncSubscriptionSetSubscription() {
      AsyncSubscription as = new AsyncSubscription();
      as.setSubscription(mockSubscription());
   }

   public void testAsyncSubscriptionSetResource() {
      AsyncSubscription as = new AsyncSubscription();
      assertTrue(as.setResource(io.reactivex.rxjava3.disposables.Disposable.empty()));
   }

   public void testAsyncSubscriptionReplaceResource() {
      AsyncSubscription as = new AsyncSubscription();
      assertTrue(as.replaceResource(io.reactivex.rxjava3.disposables.Disposable.empty()));
   }

   public void testAsyncSubscriptionWithDisposable() {
      AsyncSubscription as = new AsyncSubscription(io.reactivex.rxjava3.disposables.Disposable.empty());
      assertFalse(as.isDisposed());
   }
}
