package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;

/**
 * @since 16.2
 */
public class AbstractImmutableBeanTest {

   // Concrete subclass for testing
   static class TestBean extends AbstractImmutableBean<String> {
      TestBean(Class<?> beanClass, String name, Set<Annotation> qualifiers,
               Class<? extends Annotation> scope, Set<Class<? extends Annotation>> stereotypes,
               Set<Type> types, boolean alternative, boolean nullable,
               Set<jakarta.enterprise.inject.spi.InjectionPoint> injectionPoints, String toString) {
         super(beanClass, name, qualifiers, scope, stereotypes, types, alternative, nullable, injectionPoints, toString);
      }

      @Override
      public String create(CreationalContext<String> creationalContext) {
         return "test";
      }

      @Override
      public void destroy(String instance, CreationalContext<String> creationalContext) {
      }
   }

   @Test
   public void testDefaultValues() {
      TestBean bean = new TestBean(String.class, "myBean", null, null, null, null, false, false, null, null);

      assertEquals(String.class, bean.getBeanClass());
      assertEquals("myBean", bean.getName());
      // Default scope should be Dependent
      assertEquals(Dependent.class, bean.getScope());
      // Default qualifiers should contain @Default
      assertNotNull(bean.getQualifiers());
      assertTrue(bean.getQualifiers().stream().anyMatch(a -> a instanceof jakarta.enterprise.inject.Default));
      // Default types should contain Object and beanClass
      assertNotNull(bean.getTypes());
      assertTrue(bean.getTypes().contains(Object.class));
      assertTrue(bean.getTypes().contains(String.class));
      // Default stereotypes should be empty
      assertNotNull(bean.getStereotypes());
      assertTrue(bean.getStereotypes().isEmpty());
      // Default injection points should be empty
      assertNotNull(bean.getInjectionPoints());
      assertTrue(bean.getInjectionPoints().isEmpty());
      assertFalse(bean.isAlternative());
      assertFalse(bean.isNullable());
   }

   @Test
   public void testExplicitValues() {
      Set<Annotation> qualifiers = Set.of(AnyLiteral.INSTANCE);
      Set<Type> types = Set.of(String.class, CharSequence.class);
      Set<Class<? extends Annotation>> stereotypes = Collections.emptySet();

      TestBean bean = new TestBean(
            String.class, "explicit", qualifiers, ApplicationScoped.class,
            stereotypes, types, true, true, Collections.emptySet(), "My Custom Bean");

      assertEquals(String.class, bean.getBeanClass());
      assertEquals("explicit", bean.getName());
      assertEquals(ApplicationScoped.class, bean.getScope());
      assertTrue(bean.getQualifiers().contains(AnyLiteral.INSTANCE));
      assertTrue(bean.getTypes().contains(CharSequence.class));
      assertTrue(bean.isAlternative());
      assertTrue(bean.isNullable());
      assertEquals("My Custom Bean", bean.toString());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testNullBeanClassThrows() {
      new TestBean(null, "test", null, null, null, null, false, false, null, null);
   }

   @Test
   public void testToStringDefault() {
      TestBean bean = new TestBean(String.class, null, null, null, null, null, false, false, null, null);
      String str = bean.toString();
      assertNotNull(str);
      assertTrue(str.contains("String"));
   }

   @Test
   public void testNullName() {
      TestBean bean = new TestBean(String.class, null, null, null, null, null, false, false, null, null);
      assertNull(bean.getName());
   }

   @Test
   public void testDefensiveCopyOfQualifiers() {
      Set<Annotation> qualifiers = new java.util.HashSet<>();
      qualifiers.add(AnyLiteral.INSTANCE);
      TestBean bean = new TestBean(String.class, null, qualifiers, null, null, null, false, false, null, null);

      // Modifying original should not affect bean
      qualifiers.clear();
      assertFalse(bean.getQualifiers().isEmpty());
   }
}
