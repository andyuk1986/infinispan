package org.infinispan.metadata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.TimeUnit;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "metadata.EmbeddedMetadataTest")
public class EmbeddedMetadataTest extends AbstractInfinispanTest {

   public void testEmptyMetadata() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      assertEquals(-1, meta.lifespan());
      assertEquals(-1, meta.maxIdle());
      assertNull(meta.version());
      assertTrue(meta.isEmpty());
   }

   public void testLifespanOnly() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      assertEquals(5000, meta.lifespan());
      assertEquals(-1, meta.maxIdle());
      assertFalse(meta.isEmpty());
   }

   public void testMaxIdleOnly() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS)
            .build();
      assertEquals(-1, meta.lifespan());
      assertEquals(3000, meta.maxIdle());
      assertFalse(meta.isEmpty());
   }

   public void testBothLifespanAndMaxIdle() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .maxIdle(3000, TimeUnit.MILLISECONDS)
            .build();
      assertEquals(5000, meta.lifespan());
      assertEquals(3000, meta.maxIdle());
      assertFalse(meta.isEmpty());
   }

   public void testLifespanSeconds() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5, TimeUnit.SECONDS)
            .build();
      assertEquals(5000, meta.lifespan());
   }

   public void testMaxIdleSeconds() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .maxIdle(3, TimeUnit.SECONDS)
            .build();
      assertEquals(3000, meta.maxIdle());
   }

   public void testLifespanNegative() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(-1, TimeUnit.MILLISECONDS)
            .build();
      assertEquals(-1, meta.lifespan());
   }

   public void testEqualsEmpty() {
      Metadata m1 = new EmbeddedMetadata.Builder().build();
      Metadata m2 = new EmbeddedMetadata.Builder().build();
      assertTrue(m1.equals(m2));
      assertEquals(m1.hashCode(), m2.hashCode());
   }

   public void testEqualsLifespan() {
      Metadata m1 = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS).build();
      Metadata m2 = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS).build();
      assertTrue(m1.equals(m2));
      assertEquals(m1.hashCode(), m2.hashCode());
   }

   public void testNotEqualsLifespan() {
      Metadata m1 = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS).build();
      Metadata m2 = new EmbeddedMetadata.Builder()
            .lifespan(6000, TimeUnit.MILLISECONDS).build();
      assertFalse(m1.equals(m2));
   }

   public void testEqualsMaxIdle() {
      Metadata m1 = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      Metadata m2 = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      assertTrue(m1.equals(m2));
      assertEquals(m1.hashCode(), m2.hashCode());
   }

   public void testNotEqualsMaxIdle() {
      Metadata m1 = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      Metadata m2 = new EmbeddedMetadata.Builder()
            .maxIdle(4000, TimeUnit.MILLISECONDS).build();
      assertFalse(m1.equals(m2));
   }

   public void testEqualsBoth() {
      Metadata m1 = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      Metadata m2 = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      assertTrue(m1.equals(m2));
      assertEquals(m1.hashCode(), m2.hashCode());
   }

   public void testNotEqualsDifferentClass() {
      Metadata m1 = new EmbeddedMetadata.Builder().build();
      assertFalse(m1.equals("notMetadata"));
   }

   public void testNotEqualsNull() {
      Metadata m1 = new EmbeddedMetadata.Builder().build();
      assertFalse(m1.equals(null));
   }

   public void testEqualsIdentity() {
      Metadata m1 = new EmbeddedMetadata.Builder().build();
      assertTrue(m1.equals(m1));
   }

   public void testToStringEmpty() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      assertNotNull(meta.toString());
   }

   public void testToStringLifespan() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS).build();
      assertNotNull(meta.toString());
   }

   public void testToStringMaxIdle() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      assertNotNull(meta.toString());
   }

   public void testToStringBoth() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .maxIdle(3000, TimeUnit.MILLISECONDS).build();
      assertNotNull(meta.toString());
   }

   public void testBuilder() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      Metadata.Builder builder = meta.builder();
      assertNotNull(builder);
      Metadata rebuilt = builder.build();
      assertEquals(5000, rebuilt.lifespan());
   }

   public void testMerge() {
      Metadata base = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      Metadata override = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS)
            .build();
      Metadata.Builder builder = base.builder();
      builder.merge(override);
      Metadata merged = builder.build();
      assertEquals(5000, merged.lifespan());
      assertEquals(3000, merged.maxIdle());
   }

   public void testIsEmptyWithNegativeLifespan() {
      // Lifespan set to < 0 should be considered empty for lifespan-only metadata
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(-1, TimeUnit.MILLISECONDS)
            .build();
      assertTrue(meta.isEmpty());
   }

   public void testIsEmptyWithNegativeMaxIdle() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .maxIdle(-1, TimeUnit.MILLISECONDS)
            .build();
      assertTrue(meta.isEmpty());
   }
}
