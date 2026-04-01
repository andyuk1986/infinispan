package org.infinispan.commons.util.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link NonReentrantLock}.
 */
public class NonReentrantLockTest {

   @Test
   public void testLockAndUnlock() {
      NonReentrantLock lock = new NonReentrantLock();
      lock.lock();
      lock.unlock();
   }

   @Test
   public void testTryLockSuccess() {
      NonReentrantLock lock = new NonReentrantLock();
      assertTrue(lock.tryLock());
      lock.unlock();
   }

   @Test
   public void testLockInterruptibly() throws Exception {
      NonReentrantLock lock = new NonReentrantLock();
      lock.lockInterruptibly();
      lock.unlock();
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testNewConditionUnsupported() {
      NonReentrantLock lock = new NonReentrantLock();
      lock.newCondition();
   }

   @Test
   public void testLockUnlockTwice() {
      NonReentrantLock lock = new NonReentrantLock();
      lock.lock();
      lock.unlock();
      lock.lock();
      lock.unlock();
   }

   @Test
   public void testTryLockUnlockTryLockAgain() {
      NonReentrantLock lock = new NonReentrantLock();
      assertTrue(lock.tryLock());
      lock.unlock();
      assertTrue(lock.tryLock());
      lock.unlock();
   }
}
