package org.infinispan.notifications.cachelistener.event.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.partitionhandling.AvailabilityMode;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "notifications.cachelistener.event.impl.EventImplTest")
public class EventImplTest extends AbstractInfinispanTest {

   public void testCreateEvent() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      assertNotNull(event);
      assertEquals(Event.Type.CACHE_ENTRY_CREATED, event.getType());
      assertNull(event.getCache());
   }

   public void testGetSetPre() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isPre());
      event.setPre(true);
      assertTrue(event.isPre());
   }

   public void testGetSetKey() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getKey());
      event.setKey("myKey");
      assertEquals("myKey", event.getKey());
   }

   public void testGetSetValue() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getValue());
      event.setValue("myValue");
      assertEquals("myValue", event.getValue());
      assertEquals("myValue", event.getNewValue());
   }

   public void testGetSetOldValue() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getOldValue());
      event.setOldValue("oldVal");
      assertEquals("oldVal", event.getOldValue());
   }

   public void testGetSetNewValue() {
      EventImpl<String, String> event = new EventImpl<>();
      event.setNewValue("newVal");
      assertEquals("newVal", event.getNewValue());
   }

   public void testGetSetOriginLocal() {
      EventImpl<String, String> event = new EventImpl<>();
      assertTrue(event.isOriginLocal());
      event.setOriginLocal(false);
      assertFalse(event.isOriginLocal());
   }

   public void testGetSetTransactionSuccessful() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isTransactionSuccessful());
      event.setTransactionSuccessful(true);
      assertTrue(event.isTransactionSuccessful());
   }

   public void testGetGlobalTransactionNull() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getGlobalTransaction());
   }

   public void testGetSetSource() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getSource());
      event.setSource("source");
      assertEquals("source", event.getSource());
   }

   public void testGetGlobalTransactionNotGlobalTx() {
      EventImpl<String, String> event = new EventImpl<>();
      event.setSource("notAGlobalTransaction");
      assertNull(event.getGlobalTransaction());
   }

   public void testGetSetCreated() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isCreated());
      event.setCreated(true);
      assertTrue(event.isCreated());
   }

   public void testGetSetCommandRetried() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isCommandRetried());
      event.setCommandRetried(true);
      assertTrue(event.isCommandRetried());
   }

   public void testGetSetCurrentState() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isCurrentState());
      event.setCurrentState(true);
      assertTrue(event.isCurrentState());
   }

   public void testGetSetMetadata() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getMetadata());
      assertNull(event.getOldMetadata());
   }

   public void testGetSetAvailabilityMode() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getAvailabilityMode());
      event.setAvailabilityMode(AvailabilityMode.AVAILABLE);
      assertEquals(AvailabilityMode.AVAILABLE, event.getAvailabilityMode());
   }

   public void testGetSetAvailable() {
      EventImpl<String, String> event = new EventImpl<>();
      assertFalse(event.isAvailable());
      event.setAvailable(true);
      assertTrue(event.isAvailable());
   }

   public void testGetSetEntries() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getEntries());
      event.setEntries(Collections.singletonMap("k", "v"));
      assertEquals(1, event.getEntries().size());
   }

   public void testGetSetNewTopologyId() {
      EventImpl<String, String> event = new EventImpl<>();
      event.setNewTopologyId(42);
      assertEquals(42, event.getNewTopologyId());
   }

   public void testGetMembersAtStartNull() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNotNull(event.getMembersAtStart());
      assertTrue(event.getMembersAtStart().isEmpty());
   }

   public void testGetMembersAtEndNull() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNotNull(event.getMembersAtEnd());
      assertTrue(event.getMembersAtEnd().isEmpty());
   }

   public void testGetConsistentHashAtStartNull() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getConsistentHashAtStart());
      assertNull(event.getReadConsistentHashAtStart());
      assertNull(event.getWriteConsistentHashAtStart());
   }

   public void testGetConsistentHashAtEndNull() {
      EventImpl<String, String> event = new EventImpl<>();
      assertNull(event.getConsistentHashAtEnd());
      assertNull(event.getReadConsistentHashAtEnd());
      assertNull(event.getWriteConsistentHashAtEnd());
   }

   // ---- equals / hashCode ----

   public void testEqualsIdentity() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      assertTrue(event.equals(event));
   }

   public void testEqualsNull() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      assertFalse(event.equals(null));
   }

   public void testEqualsDifferentClass() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      assertFalse(event.equals("notAnEvent"));
   }

   public void testEqualsSameProperties() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setKey("key");
      e1.setValue("value");
      e1.setOldValue("old");
      e1.setCreated(true);
      e1.setAvailable(true);

      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setKey("key");
      e2.setValue("value");
      e2.setOldValue("old");
      e2.setCreated(true);
      e2.setAvailable(true);

      assertTrue(e1.equals(e2));
      assertEquals(e1.hashCode(), e2.hashCode());
   }

   public void testEqualsDifferentType() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_REMOVED);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentKey() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setKey("key1");
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setKey("key2");
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentPre() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setPre(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setPre(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentOriginLocal() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setOriginLocal(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setOriginLocal(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentTransactionSuccessful() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setTransactionSuccessful(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setTransactionSuccessful(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentValue() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setValue("val1");
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setValue("val2");
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentNewValue() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setNewValue("new1");
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setNewValue("new2");
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentOldValue() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setOldValue("old1");
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setOldValue("old2");
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentCreated() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setCreated(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setCreated(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentCurrentState() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setCurrentState(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setCurrentState(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentAvailable() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setAvailable(true);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setAvailable(false);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentNewTopologyId() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setNewTopologyId(1);
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setNewTopologyId(2);
      assertFalse(e1.equals(e2));
   }

   public void testEqualsDifferentSource() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setSource("src1");
      EventImpl<String, String> e2 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e2.setSource("src2");
      assertFalse(e1.equals(e2));
   }

   // ---- toString ----

   public void testToStringNormalEvent() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      event.setKey("key");
      event.setValue("value");
      String str = event.toString();
      assertNotNull(str);
      assertTrue(str.contains("CACHE_ENTRY_CREATED"));
      assertTrue(str.contains("key"));
   }

   public void testToStringTopologyChanged() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.TOPOLOGY_CHANGED);
      String str = event.toString();
      assertNotNull(str);
      assertTrue(str.contains("TOPOLOGY_CHANGED"));
   }

   public void testToStringDataRehashed() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.DATA_REHASHED);
      String str = event.toString();
      assertNotNull(str);
      assertTrue(str.contains("DATA_REHASHED"));
   }

   // ---- clone ----

   public void testClone() {
      EventImpl<String, String> event = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      event.setKey("key");
      event.setValue("value");
      EventImpl<String, String> cloned = event.clone();
      assertNotNull(cloned);
      assertEquals(event.getKey(), cloned.getKey());
      assertEquals(event.getValue(), cloned.getValue());
      assertEquals(event.getType(), cloned.getType());
      assertTrue(event.equals(cloned));
   }

   // ---- hashCode ----

   public void testHashCodeConsistency() {
      EventImpl<String, String> e1 = EventImpl.createEvent(null, Event.Type.CACHE_ENTRY_CREATED);
      e1.setKey("key");
      e1.setValue("value");
      e1.setOldValue("old");
      e1.setSource("src");

      int h1 = e1.hashCode();
      int h2 = e1.hashCode();
      assertEquals(h1, h2);
   }

   public void testHashCodeNullFields() {
      EventImpl<String, String> event = new EventImpl<>();
      // Should not throw
      event.hashCode();
   }
}
