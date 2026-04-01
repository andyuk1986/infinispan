package org.infinispan.reactive.internal.disposables;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

import io.reactivex.rxjava3.disposables.Disposable;

@Test(groups = "unit", testName = "reactive.internal.disposables.DisposableHelperTest")
public class DisposableHelperTest extends AbstractInfinispanTest {

   private Disposable trackingDisposable(AtomicBoolean flag) {
      return new Disposable() {
         @Override
         public void dispose() {
            flag.set(true);
         }

         @Override
         public boolean isDisposed() {
            return flag.get();
         }
      };
   }

   public void testIsDisposed() {
      assertTrue(DisposableHelper.isDisposed(DisposableHelper.DISPOSED));
      assertFalse(DisposableHelper.isDisposed(Disposable.empty()));
   }

   public void testSetNormal() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      Disposable d = Disposable.empty();
      assertTrue(DisposableHelper.set(field, d));
   }

   public void testSetDisposesOld() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      AtomicBoolean firstDisposed = new AtomicBoolean(false);
      Disposable first = trackingDisposable(firstDisposed);
      DisposableHelper.set(field, first);

      Disposable second = Disposable.empty();
      assertTrue(DisposableHelper.set(field, second));
      assertTrue(firstDisposed.get());
   }

   public void testSetOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      assertFalse(DisposableHelper.set(field, d));
      assertTrue(disposed.get());
   }

   public void testSetNullOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      assertFalse(DisposableHelper.set(field, null));
   }

   public void testSetOnceSuccess() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      Disposable d = Disposable.empty();
      assertTrue(DisposableHelper.setOnce(field, d));
   }

   public void testSetOnceAlreadySet() {
      AtomicReference<Disposable> field = new AtomicReference<>(Disposable.empty());
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      assertFalse(DisposableHelper.setOnce(field, d));
      assertTrue(disposed.get());
   }

   public void testSetOnceOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      assertFalse(DisposableHelper.setOnce(field, d));
      assertTrue(disposed.get());
   }

   public void testReplaceNormal() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      assertTrue(DisposableHelper.replace(field, Disposable.empty()));
   }

   public void testReplaceOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      assertFalse(DisposableHelper.replace(field, d));
      assertTrue(disposed.get());
   }

   public void testReplaceNullOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      assertFalse(DisposableHelper.replace(field, null));
   }

   public void testDisposeNormal() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      field.set(d);
      assertTrue(DisposableHelper.dispose(field));
      assertTrue(disposed.get());
   }

   public void testDisposeNull() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      assertTrue(DisposableHelper.dispose(field));
   }

   public void testDisposeAlreadyDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      assertFalse(DisposableHelper.dispose(field));
   }

   public void testValidateNormal() {
      assertTrue(DisposableHelper.validate(null, Disposable.empty()));
   }

   public void testValidateNextNull() {
      assertFalse(DisposableHelper.validate(null, null));
   }

   public void testValidateCurrentNotNull() {
      assertFalse(DisposableHelper.validate(Disposable.empty(), Disposable.empty()));
   }

   public void testTrySetSuccess() {
      AtomicReference<Disposable> field = new AtomicReference<>();
      assertTrue(DisposableHelper.trySet(field, Disposable.empty()));
   }

   public void testTrySetAlreadySet() {
      AtomicReference<Disposable> field = new AtomicReference<>(Disposable.empty());
      assertFalse(DisposableHelper.trySet(field, Disposable.empty()));
   }

   public void testTrySetOnDisposed() {
      AtomicReference<Disposable> field = new AtomicReference<>(DisposableHelper.DISPOSED);
      AtomicBoolean disposed = new AtomicBoolean(false);
      Disposable d = trackingDisposable(disposed);
      assertFalse(DisposableHelper.trySet(field, d));
      assertTrue(disposed.get());
   }

   public void testDisposedEnumDispose() {
      // Calling dispose on the DISPOSED enum itself should be no-op
      DisposableHelper.DISPOSED.dispose();
   }

   public void testDisposedEnumIsDisposed() {
      assertTrue(DisposableHelper.DISPOSED.isDisposed());
   }

   // ---- SequentialDisposable ----

   public void testSequentialDisposableUpdate() {
      SequentialDisposable sd = new SequentialDisposable();
      assertTrue(sd.update(Disposable.empty()));
   }

   public void testSequentialDisposableReplace() {
      SequentialDisposable sd = new SequentialDisposable();
      assertTrue(sd.replace(Disposable.empty()));
   }

   public void testSequentialDisposableDispose() {
      SequentialDisposable sd = new SequentialDisposable();
      assertFalse(sd.isDisposed());
      sd.dispose();
      assertTrue(sd.isDisposed());
   }

   public void testSequentialDisposableWithInitial() {
      Disposable initial = Disposable.empty();
      SequentialDisposable sd = new SequentialDisposable(initial);
      assertFalse(sd.isDisposed());
   }

   public void testSequentialDisposableUpdateAfterDispose() {
      SequentialDisposable sd = new SequentialDisposable();
      sd.dispose();
      assertFalse(sd.update(Disposable.empty()));
   }

   // ---- CancellableDisposable ----

   public void testCancellableDisposable() {
      AtomicBoolean cancelled = new AtomicBoolean(false);
      CancellableDisposable cd = new CancellableDisposable(() -> cancelled.set(true));
      assertFalse(cd.isDisposed());
      cd.dispose();
      assertTrue(cd.isDisposed());
      assertTrue(cancelled.get());
   }

   public void testCancellableDisposableDoubleDispose() {
      AtomicBoolean cancelled = new AtomicBoolean(false);
      CancellableDisposable cd = new CancellableDisposable(() -> cancelled.set(true));
      cd.dispose();
      cd.dispose(); // second dispose should be no-op
      assertTrue(cd.isDisposed());
   }
}
