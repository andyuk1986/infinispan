package org.infinispan.metadata.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.TimeUnit;

import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "metadata.impl.MetadataImplTest")
public class MetadataImplTest extends AbstractInfinispanTest {

   // ---- InternalMetadataImpl ----

   public void testInternalMetadataImplBasics() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertEquals(5000, impl.lifespan());
      assertEquals(-1, impl.maxIdle());
      assertEquals(1000, impl.created());
      assertEquals(2000, impl.lastUsed());
      assertNotNull(impl.actual());
   }

   public void testInternalMetadataImplExpiryTimeLifespanOnly() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      // maxIdle = -1, so expiry = created + lifespan = 1000 + 5000 = 6000
      assertEquals(6000, impl.expiryTime());
   }

   public void testInternalMetadataImplExpiryTimeMaxIdleOnly() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .maxIdle(3000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      // lifespan = -1, so expiry = lastUsed + maxIdle = 2000 + 3000 = 5000
      assertEquals(5000, impl.expiryTime());
   }

   public void testInternalMetadataImplExpiryTimeBoth() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .maxIdle(3000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      // lifespan expiry = 1000+5000 = 6000, maxIdle expiry = 2000+3000 = 5000, min = 5000
      assertEquals(5000, impl.expiryTime());
   }

   public void testInternalMetadataImplExpiryTimeNeither() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertEquals(-1, impl.expiryTime());
   }

   public void testInternalMetadataImplIsExpired() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertFalse(impl.isExpired(5999));
      assertTrue(impl.isExpired(6001));
   }

   public void testInternalMetadataImplIsExpiredNoExpiry() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertFalse(impl.isExpired(Long.MAX_VALUE));
   }

   public void testInternalMetadataImplEquals() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl i1 = new InternalMetadataImpl(meta, 1000, 2000);
      InternalMetadataImpl i2 = new InternalMetadataImpl(meta, 1000, 2000);
      assertTrue(i1.equals(i2));
      assertEquals(i1.hashCode(), i2.hashCode());
   }

   public void testInternalMetadataImplNotEquals() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl i1 = new InternalMetadataImpl(meta, 1000, 2000);
      InternalMetadataImpl i2 = new InternalMetadataImpl(meta, 1000, 3000);
      assertFalse(i1.equals(i2));
   }

   public void testInternalMetadataImplNotEqualsNull() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl i1 = new InternalMetadataImpl(meta, 1000, 2000);
      assertFalse(i1.equals(null));
   }

   public void testInternalMetadataImplNotEqualsDifferentClass() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl i1 = new InternalMetadataImpl(meta, 1000, 2000);
      assertFalse(i1.equals("notMetadata"));
   }

   public void testInternalMetadataImplToString() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertNotNull(impl.toString());
   }

   public void testInternalMetadataImplBuilder() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      Metadata.Builder builder = impl.builder();
      assertNotNull(builder);
      Metadata built = builder.build();
      assertNotNull(built);
   }

   public void testInternalMetadataImplVersion() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertNull(impl.version());
   }

   public void testInternalMetadataImplIsEmpty() {
      Metadata meta = new EmbeddedMetadata.Builder().build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertTrue(impl.isEmpty());
   }

   public void testInternalMetadataImplIsEmptyWithLifespan() {
      Metadata meta = new EmbeddedMetadata.Builder()
            .lifespan(5000, TimeUnit.MILLISECONDS)
            .build();
      InternalMetadataImpl impl = new InternalMetadataImpl(meta, 1000, 2000);
      assertFalse(impl.isEmpty());
   }

   // ---- PrivateMetadata ----

   public void testPrivateMetadataEmpty() {
      PrivateMetadata pm = PrivateMetadata.empty();
      assertNotNull(pm);
      assertTrue(pm.isEmpty());
      assertNull(pm.iracMetadata());
      assertNull(pm.entryVersion());
   }

   public void testPrivateMetadataEmptySingleton() {
      PrivateMetadata pm1 = PrivateMetadata.empty();
      PrivateMetadata pm2 = PrivateMetadata.empty();
      assertTrue(pm1 == pm2);
   }

   public void testPrivateMetadataBuilderFromExisting() {
      PrivateMetadata existing = PrivateMetadata.empty();
      PrivateMetadata.Builder builder = PrivateMetadata.getBuilder(existing);
      assertNotNull(builder);
      PrivateMetadata pm = builder.build();
      assertTrue(pm.isEmpty());
   }

   public void testPrivateMetadataBuilderFromNull() {
      PrivateMetadata.Builder builder = PrivateMetadata.getBuilder(null);
      assertNotNull(builder);
   }

   public void testPrivateMetadataEquals() {
      PrivateMetadata pm1 = PrivateMetadata.empty();
      PrivateMetadata pm2 = PrivateMetadata.empty();
      assertTrue(pm1.equals(pm2));
      assertEquals(pm1.hashCode(), pm2.hashCode());
   }

   public void testPrivateMetadataNotEqualsNull() {
      PrivateMetadata pm = PrivateMetadata.empty();
      assertFalse(pm.equals(null));
   }

   public void testPrivateMetadataNotEqualsDifferentClass() {
      PrivateMetadata pm = PrivateMetadata.empty();
      assertFalse(pm.equals("notPrivateMetadata"));
   }

   public void testPrivateMetadataToString() {
      PrivateMetadata pm = PrivateMetadata.empty();
      assertNotNull(pm.toString());
   }
}
