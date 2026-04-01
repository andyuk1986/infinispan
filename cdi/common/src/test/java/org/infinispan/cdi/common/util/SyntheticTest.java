package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * @since 16.2
 */
public class SyntheticTest {

   @Test
   public void testSyntheticLiteral() {
      Synthetic.SyntheticLiteral literal = new Synthetic.SyntheticLiteral("test-ns", 42L);
      assertEquals(42L, literal.index());
      assertEquals("test-ns", literal.namespace());
   }

   @Test
   public void testProviderGetTransient() {
      Synthetic.Provider provider = new Synthetic.Provider("ns1");

      Synthetic s1 = provider.get();
      Synthetic s2 = provider.get();

      assertNotNull(s1);
      assertNotNull(s2);
      // Each transient call should produce unique qualifiers
      assertNotEquals(s1.index(), s2.index());
      assertEquals("ns1", s1.namespace());
      assertEquals("ns1", s2.namespace());
   }

   @Test
   public void testProviderGetCached() {
      Synthetic.Provider provider = new Synthetic.Provider("ns2");
      Object key = "myKey";

      Synthetic s1 = provider.get(key);
      Synthetic s2 = provider.get(key);

      assertNotNull(s1);
      // Same key should return same qualifier
      assertSame(s1, s2);
      assertEquals("ns2", s1.namespace());
   }

   @Test
   public void testProviderGetCachedDifferentKeys() {
      Synthetic.Provider provider = new Synthetic.Provider("ns3");

      Synthetic s1 = provider.get("key1");
      Synthetic s2 = provider.get("key2");

      assertNotNull(s1);
      assertNotNull(s2);
      assertNotEquals(s1.index(), s2.index());
   }

   @Test
   public void testProviderClear() {
      Synthetic.Provider provider = new Synthetic.Provider("ns4");

      Synthetic s1 = provider.get("key1");
      long firstIndex = s1.index();

      provider.clear();

      // After clear, same key should get a new qualifier with different index
      Synthetic s2 = provider.get("key1");
      assertNotEquals(firstIndex, s2.index());
   }

   @Test
   public void testProviderIndexIncrementing() {
      Synthetic.Provider provider = new Synthetic.Provider("ns5");

      Synthetic s1 = provider.get();
      Synthetic s2 = provider.get();
      Synthetic s3 = provider.get();

      assertEquals(s1.index() + 1, s2.index());
      assertEquals(s2.index() + 1, s3.index());
   }
}
