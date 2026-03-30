package org.infinispan.notifications.cachemanagerlistener.event.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.infinispan.notifications.cachemanagerlistener.event.ConfigurationChangedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.Event;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "notifications.cachemanagerlistener.event.impl.CacheManagerEventImplTest")
public class CacheManagerEventImplTest extends AbstractInfinispanTest {

   // ---- EventImpl ----

   public void testEventImplDefaultConstructor() {
      EventImpl event = new EventImpl();
      assertNull(event.getCacheName());
      assertNull(event.getCacheManager());
      assertNull(event.getType());
   }

   public void testEventImplFullConstructor() {
      EventImpl event = new EventImpl("cache1", null, Event.Type.CACHE_STARTED, null, null, null, 5);
      assertEquals("cache1", event.getCacheName());
      assertEquals(Event.Type.CACHE_STARTED, event.getType());
      assertEquals(5, event.getViewId());
   }

   public void testEventImplSetters() {
      EventImpl event = new EventImpl();
      event.setCacheName("testCache");
      event.setType(Event.Type.CACHE_STOPPED);
      event.setViewId(10);
      event.setMergeView(true);

      assertEquals("testCache", event.getCacheName());
      assertEquals(Event.Type.CACHE_STOPPED, event.getType());
      assertEquals(10, event.getViewId());
      assertTrue(event.isMergeView());
   }

   public void testEventImplNewMembersNull() {
      EventImpl event = new EventImpl();
      assertNotNull(event.getNewMembers());
      assertTrue(event.getNewMembers().isEmpty());
   }

   public void testEventImplOldMembersNull() {
      EventImpl event = new EventImpl();
      assertNotNull(event.getOldMembers());
      assertTrue(event.getOldMembers().isEmpty());
   }

   public void testEventImplSubgroupsMerged() {
      EventImpl event = new EventImpl();
      event.setSubgroupsMerged(Collections.singletonList(Collections.emptyList()));
      assertNotNull(event.getSubgroupsMerged());
   }

   public void testEventImplEqualsIdentity() {
      EventImpl event = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      assertTrue(event.equals(event));
   }

   public void testEventImplEqualsNull() {
      EventImpl event = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      assertFalse(event.equals(null));
   }

   public void testEventImplEqualsDifferentClass() {
      EventImpl event = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      assertFalse(event.equals("notAnEvent"));
   }

   public void testEventImplEqualsSame() {
      EventImpl e1 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      EventImpl e2 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      assertTrue(e1.equals(e2));
      assertEquals(e1.hashCode(), e2.hashCode());
   }

   public void testEventImplEqualsDifferentType() {
      EventImpl e1 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      EventImpl e2 = new EventImpl("c", null, Event.Type.CACHE_STOPPED, null, null, null, 1);
      assertFalse(e1.equals(e2));
   }

   public void testEventImplEqualsDifferentCacheName() {
      EventImpl e1 = new EventImpl("c1", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      EventImpl e2 = new EventImpl("c2", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      assertFalse(e1.equals(e2));
   }

   public void testEventImplEqualsDifferentViewId() {
      EventImpl e1 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      EventImpl e2 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 2);
      assertFalse(e1.equals(e2));
   }

   public void testEventImplEqualsDifferentMergeView() {
      EventImpl e1 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      e1.setMergeView(true);
      EventImpl e2 = new EventImpl("c", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      e2.setMergeView(false);
      assertFalse(e1.equals(e2));
   }

   public void testEventImplToString() {
      EventImpl event = new EventImpl("cache1", null, Event.Type.CACHE_STARTED, null, null, null, 1);
      String str = event.toString();
      assertNotNull(str);
      assertTrue(str.contains("cache1"));
   }

   public void testEventImplHashCodeNullFields() {
      EventImpl event = new EventImpl();
      // Should not throw
      event.hashCode();
   }

   public void testEventImplSitesView() {
      EventImpl event = new EventImpl();
      event.setSitesView(Collections.singleton("site1"));
      event.setSitesUp(Collections.singleton("site1"));
      event.setSitesDown(Collections.emptySet());
      assertNotNull(event.getSites());
   }

   // ---- ConfigurationChangedEventImpl ----

   public void testConfigurationChangedEvent() {
      Map<String, Object> entityValue = Collections.singletonMap("key", "val");
      ConfigurationChangedEventImpl event = new ConfigurationChangedEventImpl(
            null, ConfigurationChangedEvent.EventType.CREATE, "CACHE", "testCache", entityValue
      );
      assertEquals(Event.Type.CONFIGURATION_CHANGED, event.getType());
      assertNull(event.getCacheManager());
      assertEquals(ConfigurationChangedEvent.EventType.CREATE, event.getConfigurationEventType());
      assertEquals("CACHE", event.getConfigurationEntityType());
      assertEquals("testCache", event.getConfigurationEntityName());
      assertEquals(entityValue, event.getConfigurationEntityValue());
   }
}
