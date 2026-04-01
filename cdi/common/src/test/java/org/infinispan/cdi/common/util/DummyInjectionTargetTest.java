package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 16.2
 */
public class DummyInjectionTargetTest {

   @Test
   public void testProduceReturnsNull() {
      DummyInjectionTarget<String> target = new DummyInjectionTarget<>();
      assertNull(target.produce(null));
   }

   @Test
   public void testGetInjectionPointsReturnsEmpty() {
      DummyInjectionTarget<String> target = new DummyInjectionTarget<>();
      assertNotNull(target.getInjectionPoints());
      assertTrue(target.getInjectionPoints().isEmpty());
   }

   @Test
   public void testLifecycleMethodsDoNotThrow() {
      DummyInjectionTarget<String> target = new DummyInjectionTarget<>();
      // All lifecycle methods should be no-ops
      target.inject("test", null);
      target.postConstruct("test");
      target.preDestroy("test");
      target.dispose("test");
   }
}
