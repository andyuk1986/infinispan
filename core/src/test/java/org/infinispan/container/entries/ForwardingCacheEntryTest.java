package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests for {@link ForwardingCacheEntry}.
 */
@Test(groups = "unit", testName = "container.entries.ForwardingCacheEntryTest")
public class ForwardingCacheEntryTest {

   private ForwardingCacheEntry<String, String> createForwarding(CacheEntry<String, String> delegate) {
      return new ForwardingCacheEntry<>() {
         @Override
         protected CacheEntry<String, String> delegate() {
            return delegate;
         }
      };
   }

   private TransientMortalCacheEntry createDelegate() {
      return new TransientMortalCacheEntry("key", "value", null, 5000, 10000, 1000, 500);
   }

   public void testGetKey() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals("key", fce.getKey());
   }

   public void testGetValue() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals("value", fce.getValue());
   }

   public void testSetValue() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setValue("newValue");
      assertEquals("newValue", fce.getValue());
      assertEquals("newValue", delegate.getValue());
   }

   public void testGetLifespan() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals(10000, fce.getLifespan());
   }

   public void testGetMaxIdle() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals(5000, fce.getMaxIdle());
   }

   public void testGetCreated() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals(500, fce.getCreated());
   }

   public void testGetLastUsed() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertEquals(1000, fce.getLastUsed());
   }

   public void testIsNull() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      assertFalse(fce.isNull());
   }

   public void testIsChangedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.isChanged(), fce.isChanged());
   }

   public void testIsCreatedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.isCreated(), fce.isCreated());
   }

   public void testIsRemovedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.isRemoved(), fce.isRemoved());
   }

   public void testIsEvictedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.isEvicted(), fce.isEvicted());
   }

   public void testIsInvalidatedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.isInvalidated(), fce.isInvalidated());
   }

   public void testSkipLookupDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.skipLookup(), fce.skipLookup());
   }

   public void testSetChangedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setChanged(true);
      assertEquals(delegate.isChanged(), fce.isChanged());
   }

   public void testSetCreatedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setCreated(true);
      assertEquals(delegate.isCreated(), fce.isCreated());
   }

   public void testSetRemovedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setRemoved(true);
      assertEquals(delegate.isRemoved(), fce.isRemoved());
   }

   public void testSetEvictedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setEvicted(true);
      assertEquals(delegate.isEvicted(), fce.isEvicted());
   }

   public void testSetInvalidatedDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setInvalidated(true);
      assertEquals(delegate.isInvalidated(), fce.isInvalidated());
   }

   public void testSetSkipLookupDelegates() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      fce.setSkipLookup(true);
      assertEquals(delegate.skipLookup(), fce.skipLookup());
   }

   public void testGetMetadata() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.getMetadata(), fce.getMetadata());
   }

   public void testClone() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      CacheEntry<String, String> cloned = fce.clone();
      assertNotNull(cloned);
      assertEquals("key", cloned.getKey());
      assertEquals("value", cloned.getValue());
   }

   public void testToString() {
      ForwardingCacheEntry<String, String> fce = createForwarding(createDelegate());
      String str = fce.toString();
      assertNotNull(str);
   }

   public void testEquals() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertTrue(fce.equals(delegate));
   }

   public void testHashCode() {
      TransientMortalCacheEntry delegate = createDelegate();
      ForwardingCacheEntry<String, String> fce = createForwarding(delegate);
      assertEquals(delegate.hashCode(), fce.hashCode());
   }
}
