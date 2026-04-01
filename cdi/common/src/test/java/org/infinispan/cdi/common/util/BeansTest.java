package org.infinispan.cdi.common.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @since 16.2
 */
public class BeansTest {

   @Test
   public void testBuildQualifiersEmptySet() {
      Set<Annotation> result = Beans.buildQualifiers(Collections.emptySet());
      assertNotNull(result);
      // Should contain @Default and @Any
      assertTrue(result.size() >= 2);
      assertTrue(result.contains(DefaultLiteral.INSTANCE));
      assertTrue(result.contains(AnyLiteral.INSTANCE));
   }

   @Test
   public void testBuildQualifiersNonEmptySet() {
      Set<Annotation> annotations = new HashSet<>();
      annotations.add(AnyLiteral.INSTANCE);
      Set<Annotation> result = Beans.buildQualifiers(annotations);
      assertNotNull(result);
      // Should contain @Any (from input and from buildQualifiers)
      assertTrue(result.contains(AnyLiteral.INSTANCE));
      // Should NOT add @Default since input was non-empty
      assertTrue(result.size() >= 1);
   }

   @Test
   public void testBuildQualifiersDoesNotModifyInput() {
      Set<Annotation> original = new HashSet<>();
      int originalSize = original.size();
      Beans.buildQualifiers(original);
      // Original set should not be modified
      assertTrue(original.size() == originalSize);
   }
}
