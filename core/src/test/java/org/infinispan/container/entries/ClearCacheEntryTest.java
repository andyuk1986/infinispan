package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests for {@link ClearCacheEntry}.
 */
@Test(groups = "unit", testName = "container.entries.ClearCacheEntryTest")
public class ClearCacheEntryTest {

   public void testGetInstance() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      ClearCacheEntry<String, String> entry2 = ClearCacheEntry.getInstance();
      assertTrue(entry == entry2);
   }

   public void testIsNull() {
      assertTrue(ClearCacheEntry.getInstance().isNull());
   }

   public void testIsChanged() {
      assertTrue(ClearCacheEntry.getInstance().isChanged());
   }

   public void testIsCreated() {
      assertFalse(ClearCacheEntry.getInstance().isCreated());
   }

   public void testIsRemoved() {
      assertTrue(ClearCacheEntry.getInstance().isRemoved());
   }

   public void testIsEvicted() {
      assertFalse(ClearCacheEntry.getInstance().isEvicted());
   }

   public void testIsInvalidated() {
      assertFalse(ClearCacheEntry.getInstance().isInvalidated());
   }

   public void testGetKey() {
      assertNull(ClearCacheEntry.getInstance().getKey());
   }

   public void testGetValue() {
      assertNull(ClearCacheEntry.getInstance().getValue());
   }

   public void testGetLifespan() {
      assertEquals(-1, ClearCacheEntry.getInstance().getLifespan());
   }

   public void testGetMaxIdle() {
      assertEquals(-1, ClearCacheEntry.getInstance().getMaxIdle());
   }

   public void testSkipLookup() {
      assertTrue(ClearCacheEntry.getInstance().skipLookup());
   }

   public void testSetValueNoOp() {
      assertNull(ClearCacheEntry.getInstance().setValue("val"));
   }

   public void testGetMetadata() {
      assertNull(ClearCacheEntry.getInstance().getMetadata());
   }

   public void testSetChangedNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setChanged(false);
      assertTrue(entry.isChanged());
   }

   public void testSetCreatedNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setCreated(true);
      assertFalse(entry.isCreated());
   }

   public void testSetRemovedNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setRemoved(false);
      assertTrue(entry.isRemoved());
   }

   public void testSetEvictedNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setEvicted(true);
      assertFalse(entry.isEvicted());
   }

   public void testSetInvalidatedNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setInvalidated(true);
      assertFalse(entry.isInvalidated());
   }

   public void testSetSkipLookupNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setSkipLookup(false);
      assertTrue(entry.skipLookup());
   }

   public void testSetMetadataNoOp() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      entry.setMetadata(null);
      assertNull(entry.getMetadata());
   }

   public void testCloneReturnsSingleton() {
      ClearCacheEntry<String, String> entry = ClearCacheEntry.getInstance();
      assertTrue(entry == entry.clone());
   }

   public void testToString() {
      assertEquals("ClearCacheEntry{}", ClearCacheEntry.getInstance().toString());
   }
}
