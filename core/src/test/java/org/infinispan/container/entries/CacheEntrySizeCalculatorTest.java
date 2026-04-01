package org.infinispan.container.entries;

import static org.testng.AssertJUnit.assertTrue;

import org.infinispan.commons.util.EntrySizeCalculator;
import org.infinispan.container.entries.metadata.MetadataImmortalCacheEntry;
import org.infinispan.container.entries.metadata.MetadataMortalCacheEntry;
import org.infinispan.container.entries.metadata.MetadataTransientCacheEntry;
import org.infinispan.container.entries.metadata.MetadataTransientMortalCacheEntry;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.testng.annotations.Test;

/**
 * Tests for {@link CacheEntrySizeCalculator}.
 */
@Test(groups = "unit", testName = "container.entries.CacheEntrySizeCalculatorTest")
public class CacheEntrySizeCalculatorTest {

   private static final EntrySizeCalculator<Object, Object> FIXED_CALCULATOR = (key, value) -> 16L;

   private CacheEntrySizeCalculator<Object, Object> createCalculator() {
      return new CacheEntrySizeCalculator<>(FIXED_CALCULATOR);
   }

   public void testImmortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> entry = new ImmortalCacheEntry("key", "value");
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> entry = new MortalCacheEntry("key", "value", null, 5000, 1000);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testTransientCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> entry = new TransientCacheEntry("key", "value", null, 5000, 1000);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testTransientMortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> entry = new TransientMortalCacheEntry("key", "value", null, 5000, 10000, 1000, 500);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMetadataImmortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      Metadata metadata = new EmbeddedMetadata.Builder().build();
      InternalCacheEntry<Object, Object> entry = new MetadataImmortalCacheEntry("key", "value", metadata);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMetadataMortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(5000).build();
      InternalCacheEntry<Object, Object> entry = new MetadataMortalCacheEntry("key", "value", metadata, 1000);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMetadataTransientCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      Metadata metadata = new EmbeddedMetadata.Builder().maxIdle(3000).build();
      InternalCacheEntry<Object, Object> entry = new MetadataTransientCacheEntry("key", "value", metadata, 1000);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMetadataTransientMortalCacheEntry() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(5000).maxIdle(3000).build();
      InternalCacheEntry<Object, Object> entry = new MetadataTransientMortalCacheEntry("key", "value", metadata, 1000, 500);
      long size = calc.calculateSize("key", entry);
      assertTrue(size > 0);
   }

   public void testMortalEntryLargerThanImmortal() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> immortal = new ImmortalCacheEntry("key", "value");
      InternalCacheEntry<Object, Object> mortal = new MortalCacheEntry("key", "value", null, 5000, 1000);
      long immortalSize = calc.calculateSize("key", immortal);
      long mortalSize = calc.calculateSize("key", mortal);
      assertTrue(mortalSize > immortalSize);
   }

   public void testTransientMortalLargerThanTransient() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      InternalCacheEntry<Object, Object> transientEntry = new TransientCacheEntry("key", "value", null, 5000, 1000);
      InternalCacheEntry<Object, Object> transientMortal = new TransientMortalCacheEntry("key", "value", null, 5000, 10000, 1000, 500);
      long transientSize = calc.calculateSize("key", transientEntry);
      long transientMortalSize = calc.calculateSize("key", transientMortal);
      assertTrue(transientMortalSize > transientSize);
   }

   public void testCalculateSizeWithNullMetadata() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      long size = calc.calculateSize("key", "value", null, null);
      assertTrue(size > 0);
   }

   public void testCalculateSizeWithEmbeddedMetadata() {
      CacheEntrySizeCalculator<Object, Object> calc = createCalculator();
      Metadata metadata = new EmbeddedMetadata.Builder().lifespan(5000).build();
      long size = calc.calculateSize("key", "value", metadata, null);
      assertTrue(size > 0);
   }
}
