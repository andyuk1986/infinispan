package org.infinispan.reactive;

import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

@Test(groups = "unit", testName = "reactive.FlowableCreateTest")
public class FlowableCreateTest extends AbstractInfinispanTest {

   public void testBufferStrategy() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         for (int i = 0; i < 5; i++) {
            emitter.onNext(i);
         }
         emitter.onComplete();
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValues(0, 1, 2, 3, 4);
   }

   public void testErrorStrategy() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.ERROR);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValue(1);
   }

   public void testDropStrategy() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.DROP);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValue(1);
   }

   public void testLatestStrategy() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.LATEST);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValue(1);
   }

   public void testMissingStrategy() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.MISSING);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValue(1);
   }

   public void testErrorInSource() {
      RuntimeException exception = new RuntimeException("test error");
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onError(exception);
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(exception);
   }

   public void testCancellation() {
      AtomicBoolean cancelled = new AtomicBoolean(false);
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.setCancellable(() -> cancelled.set(true));
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.cancel();
      assertTrue(cancelled.get());
   }

   public void testErrorStrategyWithOverflow() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.ERROR);

      TestSubscriber<Integer> subscriber = flowable.test(0);
      subscriber.request(1);
      subscriber.assertValue(1);
   }

   public void testDropStrategyWithOverflow() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.DROP);

      TestSubscriber<Integer> subscriber = flowable.test(0);
      subscriber.request(1);
      subscriber.assertValue(1);
   }

   public void testLatestStrategyMultipleValues() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         for (int i = 0; i < 3; i++) {
            emitter.onNext(i);
         }
         emitter.onComplete();
      }, BackpressureStrategy.LATEST);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValues(0, 1, 2);
   }

   public void testBufferWithErrorSignal() {
      RuntimeException exception = new RuntimeException("buffer error");
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         emitter.onError(exception);
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(exception);
      subscriber.assertValue(1);
   }

   public void testLatestWithErrorSignal() {
      RuntimeException exception = new RuntimeException("latest error");
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onError(exception);
      }, BackpressureStrategy.LATEST);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(exception);
   }

   public void testMissingStrategyNullValue() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(null);
      }, BackpressureStrategy.MISSING);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(NullPointerException.class);
   }

   public void testDropStrategyNullValue() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(null);
      }, BackpressureStrategy.DROP);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(NullPointerException.class);
   }

   public void testErrorStrategyNullValue() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(null);
      }, BackpressureStrategy.ERROR);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(NullPointerException.class);
   }

   public void testBufferStrategyNullValue() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(null);
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(NullPointerException.class);
   }

   public void testLatestStrategyNullValue() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(null);
      }, BackpressureStrategy.LATEST);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(NullPointerException.class);
   }

   public void testSerializedEmitter() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         var serialized = emitter.serialize();
         serialized.onNext(1);
         serialized.onNext(2);
         serialized.onComplete();
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertValues(1, 2);
   }

   public void testSerializedEmitterError() {
      RuntimeException exception = new RuntimeException("serialized error");
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         var serialized = emitter.serialize();
         serialized.onError(exception);
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertError(exception);
   }

   public void testSerializedEmitterComplete() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         var serialized = emitter.serialize();
         serialized.onComplete();
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
   }

   public void testRequestedAmount() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         assertTrue(emitter.requested() > 0);
         emitter.onNext(1);
         emitter.onComplete();
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
   }

   public void testIsCancelledAfterCancel() {
      AtomicBoolean wasCancelled = new AtomicBoolean(false);
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onNext(1);
         if (!emitter.isCancelled()) {
            emitter.onComplete();
         }
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.cancel();
   }

   public void testBufferStrategyCompleteAfterDone() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onComplete();
         // calling onNext after complete should be ignored
         emitter.onNext(999);
      }, BackpressureStrategy.BUFFER);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertNoValues();
   }

   public void testLatestStrategyCompleteAfterDone() {
      Flowable<Integer> flowable = new FlowableCreate<>(emitter -> {
         emitter.onComplete();
         emitter.onNext(999);
      }, BackpressureStrategy.LATEST);

      TestSubscriber<Integer> subscriber = flowable.test();
      subscriber.assertComplete();
      subscriber.assertNoValues();
   }
}
