package org.infinispan.notifications.cachelistener.filter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "notifications.cachelistener.filter.EventTypeTest")
public class EventTypeTest extends AbstractInfinispanTest {

   public void testCreateEvent() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_CREATED);
      assertTrue(et.isCreate());
      assertFalse(et.isModified());
      assertFalse(et.isRemove());
      assertFalse(et.isExpired());
      assertEquals(Event.Type.CACHE_ENTRY_CREATED, et.getType());
   }

   public void testModifiedEvent() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_MODIFIED);
      assertFalse(et.isCreate());
      assertTrue(et.isModified());
      assertFalse(et.isRemove());
      assertFalse(et.isExpired());
   }

   public void testRemoveEvent() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_REMOVED);
      assertFalse(et.isCreate());
      assertFalse(et.isModified());
      assertTrue(et.isRemove());
      assertFalse(et.isExpired());
   }

   public void testExpiredEvent() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_EXPIRED);
      assertFalse(et.isCreate());
      assertFalse(et.isModified());
      assertFalse(et.isRemove());
      assertTrue(et.isExpired());
   }

   public void testPreEvent() {
      EventType et = new EventType(false, true, Event.Type.CACHE_ENTRY_CREATED);
      assertTrue(et.isPreEvent());
   }

   public void testNotPreEvent() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_CREATED);
      assertFalse(et.isPreEvent());
   }

   public void testRetried() {
      EventType et = new EventType(true, false, Event.Type.CACHE_ENTRY_CREATED);
      assertTrue(et.isRetry());
   }

   public void testNotRetried() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_CREATED);
      assertFalse(et.isRetry());
   }

   public void testOtherEventType() {
      EventType et = new EventType(false, false, Event.Type.CACHE_ENTRY_VISITED);
      assertFalse(et.isCreate());
      assertFalse(et.isModified());
      assertFalse(et.isRemove());
      assertFalse(et.isExpired());
   }
}
