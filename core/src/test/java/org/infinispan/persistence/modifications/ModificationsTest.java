package org.infinispan.persistence.modifications;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "persistence.modifications.ModificationsTest")
public class ModificationsTest extends AbstractInfinispanTest {

   // ---- Clear ----

   public void testClearGetType() {
      Clear clear = new Clear();
      assertEquals(Modification.Type.CLEAR, clear.getType());
   }

   // ---- Remove ----

   public void testRemoveGetType() {
      Remove remove = new Remove("key1");
      assertEquals(Modification.Type.REMOVE, remove.getType());
   }

   public void testRemoveGetKey() {
      Remove remove = new Remove("myKey");
      assertEquals("myKey", remove.getKey());
   }

   public void testRemoveEqualsIdentity() {
      Remove remove = new Remove("key1");
      assertTrue(remove.equals(remove));
   }

   public void testRemoveEqualsNull() {
      Remove remove = new Remove("key1");
      assertFalse(remove.equals(null));
   }

   public void testRemoveEqualsDifferentClass() {
      Remove remove = new Remove("key1");
      assertFalse(remove.equals("notARemove"));
   }

   public void testRemoveEqualsSameKey() {
      Remove r1 = new Remove("key1");
      Remove r2 = new Remove("key1");
      assertTrue(r1.equals(r2));
   }

   public void testRemoveEqualsDifferentKey() {
      Remove r1 = new Remove("key1");
      Remove r2 = new Remove("key2");
      assertFalse(r1.equals(r2));
   }

   public void testRemoveEqualsNullKeys() {
      Remove r1 = new Remove(null);
      Remove r2 = new Remove(null);
      assertTrue(r1.equals(r2));
   }

   public void testRemoveEqualsOneNullKey() {
      Remove r1 = new Remove(null);
      Remove r2 = new Remove("key1");
      assertFalse(r1.equals(r2));

      Remove r3 = new Remove("key1");
      Remove r4 = new Remove(null);
      assertFalse(r3.equals(r4));
   }

   public void testRemoveHashCodeWithKey() {
      Remove remove = new Remove("key1");
      assertEquals("key1".hashCode(), remove.hashCode());
   }

   public void testRemoveHashCodeNullKey() {
      Remove remove = new Remove(null);
      assertEquals(0, remove.hashCode());
   }

   public void testRemoveToString() {
      Remove remove = new Remove("key1");
      String str = remove.toString();
      assertNotNull(str);
      assertTrue(str.contains("key1"));
   }

   // ---- Store ----

   public void testStoreGetType() {
      Store store = new Store("key1", null);
      assertEquals(Modification.Type.STORE, store.getType());
   }

   public void testStoreGetKey() {
      Store store = new Store("myKey", null);
      assertEquals("myKey", store.getKey());
   }

   public void testStoreGetStoredValue() {
      Store store = new Store("key1", null);
      assertEquals(null, store.getStoredValue());
   }

   public void testStoreEqualsIdentity() {
      Store store = new Store("key1", null);
      assertTrue(store.equals(store));
   }

   public void testStoreEqualsNull() {
      Store store = new Store("key1", null);
      assertFalse(store.equals(null));
   }

   public void testStoreEqualsDifferentClass() {
      Store store = new Store("key1", null);
      assertFalse(store.equals("notAStore"));
   }

   public void testStoreEqualsSameKeyNullEntries() {
      Store s1 = new Store("key1", null);
      Store s2 = new Store("key1", null);
      assertTrue(s1.equals(s2));
   }

   public void testStoreEqualsDifferentKeys() {
      Store s1 = new Store("key1", null);
      Store s2 = new Store("key2", null);
      assertFalse(s1.equals(s2));
   }

   public void testStoreEqualsNullKey() {
      Store s1 = new Store(null, null);
      Store s2 = new Store(null, null);
      assertTrue(s1.equals(s2));
   }

   public void testStoreEqualsOneNullKey() {
      Store s1 = new Store(null, null);
      Store s2 = new Store("key1", null);
      assertFalse(s1.equals(s2));
   }

   public void testStoreHashCodeWithKey() {
      Store store = new Store("key1", null);
      int expected = "key1".hashCode();
      // storedEntry is null so second part is 0
      expected = 31 * expected + 0;
      assertEquals(expected, store.hashCode());
   }

   public void testStoreHashCodeNullKey() {
      Store store = new Store(null, null);
      assertEquals(0, store.hashCode());
   }

   public void testStoreToString() {
      Store store = new Store("key1", null);
      String str = store.toString();
      assertNotNull(str);
      assertTrue(str.contains("key1"));
   }

   // ---- ModificationsList ----

   public void testModificationsListGetType() {
      ModificationsList list = new ModificationsList(Collections.emptyList());
      assertEquals(Modification.Type.LIST, list.getType());
   }

   public void testModificationsListGetList() {
      List<Modification> mods = Arrays.asList(new Clear(), new Remove("key1"));
      ModificationsList list = new ModificationsList(mods);
      assertEquals(mods, list.getList());
   }

   public void testModificationsListEqualsIdentity() {
      ModificationsList list = new ModificationsList(Collections.emptyList());
      assertTrue(list.equals(list));
   }

   public void testModificationsListEqualsNull() {
      ModificationsList list = new ModificationsList(Collections.emptyList());
      assertFalse(list.equals(null));
   }

   public void testModificationsListEqualsDifferentClass() {
      ModificationsList list = new ModificationsList(Collections.emptyList());
      assertFalse(list.equals("notAList"));
   }

   public void testModificationsListEqualsSame() {
      List<Modification> mods = Arrays.asList(new Clear());
      ModificationsList l1 = new ModificationsList(mods);
      ModificationsList l2 = new ModificationsList(mods);
      assertTrue(l1.equals(l2));
   }

   public void testModificationsListEqualsDifferent() {
      ModificationsList l1 = new ModificationsList(Arrays.asList(new Clear()));
      ModificationsList l2 = new ModificationsList(Arrays.asList(new Remove("k")));
      assertFalse(l1.equals(l2));
   }

   public void testModificationsListHashCode() {
      List<Modification> mods = Arrays.asList(new Clear());
      ModificationsList list = new ModificationsList(mods);
      assertNotNull(list.hashCode());
   }

   public void testModificationsListToString() {
      ModificationsList list = new ModificationsList(Collections.emptyList());
      String str = list.toString();
      assertNotNull(str);
      assertTrue(str.contains("ModificationsList"));
   }
}
