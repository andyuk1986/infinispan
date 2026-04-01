package org.infinispan.functional.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;

import org.infinispan.container.versioning.NumericVersion;
import org.infinispan.functional.MetaParam;
import org.infinispan.functional.MetaParam.MetaCreated;
import org.infinispan.functional.MetaParam.MetaEntryVersion;
import org.infinispan.functional.MetaParam.MetaLastUsed;
import org.infinispan.functional.MetaParam.MetaLifespan;
import org.infinispan.functional.MetaParam.MetaMaxIdle;
import org.testng.annotations.Test;

/**
 * Unit test for metadata parameters collection.
 */
@Test(groups = "functional", testName = "functional.impl.MetaParamsTest")
public class MetaParamsTest {

   public void testEmptyMetaParamsFind() {
      MetaParams metas = MetaParams.empty();
      assertTrue(metas.isEmpty());
      assertEquals(0, metas.size());
      assertFalse(metas.find(MetaLifespan.class).isPresent());
      assertFalse(metas.find(MetaEntryVersion.class).isPresent());
      assertFalse(metas.find(MetaMaxIdle.class).isPresent());
   }

   @Test
   public void testAddFindMetaParam() {
      MetaParams metas = MetaParams.empty();
      MetaLifespan lifespan = new MetaLifespan(1000);
      metas.add(lifespan);
      assertFalse(metas.isEmpty());
      assertEquals(1, metas.size());
      Optional<MetaLifespan> lifespanFound = metas.find(MetaLifespan.class);
      assertEquals(new MetaLifespan(1000), lifespanFound.get());
      assertEquals(1000, metas.find(MetaLifespan.class).get().get().longValue());
      assertFalse(new MetaLifespan(900).equals(lifespanFound.get()));
      metas.add(new MetaLifespan(900));
      assertFalse(metas.isEmpty());
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaLifespan(900)), metas.find(lifespan.getClass()));
   }

   @Test
   public void testAddFindMultipleMetaParams() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(1000), new MetaEntryVersion(new NumericVersion(12345)));
      assertFalse(metas.isEmpty());
      assertEquals(3, metas.size());
      Optional<MetaMaxIdle> maxIdle = metas.find(MetaMaxIdle.class);
      Optional<MetaEntryVersion> entryVersion = metas.find(MetaEntryVersion.class);
      assertEquals(Optional.of(new MetaMaxIdle(1000)), maxIdle);
      assertFalse(900 == maxIdle.get().get().longValue());
      assertEquals(new MetaEntryVersion(new NumericVersion(12345)), entryVersion.get());
      assertFalse(new MetaEntryVersion(new NumericVersion(23456)).equals(entryVersion.get()));
   }

   @Test
   public void testReplaceFindMultipleMetaParams() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(1000), new MetaEntryVersion(new NumericVersion(12345)));
      assertFalse(metas.isEmpty());
      assertEquals(3, metas.size());
      metas.addMany(new MetaLifespan(2000), new MetaMaxIdle(2000));
      assertFalse(metas.isEmpty());
      assertEquals(3, metas.size());
      assertEquals(Optional.of(new MetaMaxIdle(2000)), metas.find(MetaMaxIdle.class));
      assertEquals(Optional.of(new MetaLifespan(2000)), metas.find(MetaLifespan.class));
      assertEquals(Optional.of(new MetaEntryVersion(new NumericVersion(12345))),
         metas.find(MetaEntryVersion.class));
   }

   @Test
   public void testConstructors() {
      MetaParams metasOf1 = MetaParams.of(new MetaCreated(1000));
      assertFalse(metasOf1.isEmpty());
      assertEquals(1, metasOf1.size());
      MetaParams metasOf2 = MetaParams.of(new MetaCreated(1000), new MetaLastUsed(2000));
      assertFalse(metasOf2.isEmpty());
      assertEquals(2, metasOf2.size());
      MetaParams metasOf4 = MetaParams.of(
         new MetaCreated(1000), new MetaLastUsed(2000), new MetaLifespan(3000), new MetaMaxIdle(4000));
      assertFalse(metasOf4.isEmpty());
      assertEquals(4, metasOf4.size());
   }

   @Test
   public void testDuplicateParametersOnConstruction() {
      MetaEntryVersion versionParam1 = new MetaEntryVersion(new NumericVersion(100));
      MetaEntryVersion versionParam2 = new MetaEntryVersion(new NumericVersion(200));
      MetaParams metas = MetaParams.of(versionParam1, versionParam2);
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaEntryVersion(new NumericVersion(200))),
         metas.find(MetaEntryVersion.class));
   }

   @Test
   public void testDuplicateParametersOnAdd() {
      MetaEntryVersion versionParam1 = new MetaEntryVersion(new NumericVersion(100));
      MetaParams metas = MetaParams.of(versionParam1);
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaEntryVersion(new NumericVersion(100))),
         metas.find(MetaEntryVersion.class));

      MetaEntryVersion versionParam2 = new MetaEntryVersion(new NumericVersion(200));
      metas.add(versionParam2);
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaEntryVersion(new NumericVersion(200))),
         metas.find(MetaEntryVersion.class));

      MetaEntryVersion versionParam3 = new MetaEntryVersion(new NumericVersion(300));
      MetaEntryVersion versionParam4 = new MetaEntryVersion(new NumericVersion(400));
      metas.addMany(versionParam3, versionParam4);
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaEntryVersion(new NumericVersion(400))),
         metas.find(MetaEntryVersion.class));
   }

   @Test
   public void testRemoveMetaParam() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      assertEquals(2, metas.size());
      metas.remove(MetaLifespan.class);
      assertEquals(1, metas.size());
      assertFalse(metas.find(MetaLifespan.class).isPresent());
      assertTrue(metas.find(MetaMaxIdle.class).isPresent());
   }

   @Test
   public void testRemoveNonExistentMetaParam() {
      MetaParams metas = MetaParams.empty();
      metas.add(new MetaLifespan(1000));
      assertEquals(1, metas.size());
      metas.remove(MetaMaxIdle.class);
      assertEquals(1, metas.size());
   }

   @Test
   public void testReplaceExistingMetaParam() {
      MetaParams metas = MetaParams.empty();
      metas.add(new MetaLifespan(1000));
      metas.replace(MetaLifespan.class, existing -> {
         assertNotNull(existing);
         assertEquals(1000, existing.get().longValue());
         return new MetaLifespan(2000);
      });
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaLifespan(2000)), metas.find(MetaLifespan.class));
   }

   @Test
   public void testReplaceNonExistentCreatesNew() {
      MetaParams metas = MetaParams.empty();
      metas.replace(MetaLifespan.class, existing -> {
         // existing should be null since it doesn't exist
         return new MetaLifespan(3000);
      });
      assertEquals(1, metas.size());
      assertEquals(Optional.of(new MetaLifespan(3000)), metas.find(MetaLifespan.class));
   }

   @Test
   public void testReplaceNonExistentReturnsNull() {
      MetaParams metas = MetaParams.empty();
      metas.replace(MetaLifespan.class, existing -> null);
      assertEquals(0, metas.size());
   }

   @Test
   public void testReplaceExistingToNull() {
      MetaParams metas = MetaParams.empty();
      metas.add(new MetaLifespan(1000));
      assertEquals(1, metas.size());
      metas.replace(MetaLifespan.class, existing -> null);
      assertEquals(0, metas.size());
      assertFalse(metas.find(MetaLifespan.class).isPresent());
   }

   @Test
   public void testReplaceUsesHoleAfterRemove() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      metas.remove(MetaLifespan.class);
      // Now there's a hole where MetaLifespan was
      metas.replace(MetaCreated.class, existing -> new MetaCreated(5000));
      assertEquals(2, metas.size());
      assertEquals(Optional.of(new MetaCreated(5000)), metas.find(MetaCreated.class));
   }

   @Test
   public void testMerge() {
      MetaParams metas1 = MetaParams.empty();
      metas1.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      MetaParams metas2 = MetaParams.empty();
      metas2.add(new MetaLifespan(500));
      // Merge: metas1's values should take precedence over metas2's for same types
      metas1.merge(metas2);
      assertEquals(Optional.of(new MetaLifespan(1000)), metas1.find(MetaLifespan.class));
      assertEquals(Optional.of(new MetaMaxIdle(2000)), metas1.find(MetaMaxIdle.class));
   }

   @Test
   public void testCopy() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      MetaParams copy = metas.copy();
      assertEquals(2, copy.size());
      assertEquals(Optional.of(new MetaLifespan(1000)), copy.find(MetaLifespan.class));
      assertEquals(Optional.of(new MetaMaxIdle(2000)), copy.find(MetaMaxIdle.class));
      // Modifying copy should not affect original
      copy.add(new MetaCreated(3000));
      assertFalse(metas.find(MetaCreated.class).isPresent());
   }

   @Test
   public void testCopyEmpty() {
      MetaParams metas = MetaParams.empty();
      MetaParams copy = metas.copy();
      assertTrue(copy.isEmpty());
   }

   @Test
   public void testIterator() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      Iterator<MetaParam<?>> it = metas.iterator();
      assertTrue(it.hasNext());
      assertNotNull(it.next());
      assertTrue(it.hasNext());
      assertNotNull(it.next());
      assertFalse(it.hasNext());
   }

   @Test(expectedExceptions = NoSuchElementException.class)
   public void testIteratorNoSuchElement() {
      MetaParams metas = MetaParams.empty();
      Iterator<MetaParam<?>> it = metas.iterator();
      it.next();
   }

   @Test
   public void testIteratorSkipsNulls() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000), new MetaCreated(3000));
      metas.remove(MetaMaxIdle.class);
      // Iterator should skip the null hole
      int count = 0;
      for (MetaParam<?> meta : metas) {
         assertNotNull(meta);
         count++;
      }
      assertEquals(2, count);
   }

   @Test
   public void testSpliterator() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      Spliterator<MetaParam<?>> spliterator = metas.spliterator();
      assertNotNull(spliterator);
      assertEquals(2, spliterator.estimateSize());
   }

   @Test
   public void testToString() {
      MetaParams metas = MetaParams.empty();
      metas.add(new MetaLifespan(1000));
      String str = metas.toString();
      assertNotNull(str);
      assertTrue(str.contains("length=1"));
   }

   @Test
   public void testAddAfterRemoveUsesHole() {
      MetaParams metas = MetaParams.empty();
      metas.addMany(new MetaLifespan(1000), new MetaMaxIdle(2000));
      metas.remove(MetaLifespan.class);
      assertEquals(1, metas.size());
      // Adding a new type should reuse the hole
      metas.add(new MetaCreated(3000));
      assertEquals(2, metas.size());
      assertEquals(Optional.of(new MetaCreated(3000)), metas.find(MetaCreated.class));
   }

}
