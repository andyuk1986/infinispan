package org.infinispan.commons.marshall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link MarshallableTypeHints}.
 */
public class MarshallableTypeHintsTest {

   @Test
   public void testGetBufferSizePredictorByClass() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      BufferSizePredictor predictor = hints.getBufferSizePredictor(String.class);
      assertNotNull(predictor);
      assertTrue(predictor.nextSize(null) > 0);
   }

   @Test
   public void testGetBufferSizePredictorReturnsSameInstance() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      BufferSizePredictor p1 = hints.getBufferSizePredictor(String.class);
      BufferSizePredictor p2 = hints.getBufferSizePredictor(String.class);
      assertTrue(p1 == p2);
   }

   @Test
   public void testGetBufferSizePredictorByObject() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      BufferSizePredictor predictor = hints.getBufferSizePredictor("hello");
      assertNotNull(predictor);
   }

   @Test
   public void testGetBufferSizePredictorByNullObject() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      BufferSizePredictor predictor = hints.getBufferSizePredictor((Object) null);
      assertNotNull(predictor);
      // NullBufferSizePredictor returns 1
      assertTrue(predictor.nextSize(null) == 1);
   }

   @Test
   public void testIsKnownMarshallableUnknown() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      assertFalse(hints.isKnownMarshallable(String.class));
   }

   @Test
   public void testIsKnownMarshallableAfterPredictor() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.getBufferSizePredictor(String.class);
      // After getting predictor, isMarshallable is still null
      assertFalse(hints.isKnownMarshallable(String.class));
   }

   @Test
   public void testIsKnownMarshallableAfterMark() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, true);
      assertTrue(hints.isKnownMarshallable(String.class));
   }

   @Test
   public void testIsMarshallableDefault() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      assertFalse(hints.isMarshallable(String.class));
   }

   @Test
   public void testMarkMarshallableTrue() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, true);
      assertTrue(hints.isMarshallable(String.class));
   }

   @Test
   public void testMarkMarshallableFalse() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, false);
      assertTrue(hints.isKnownMarshallable(String.class));
      assertFalse(hints.isMarshallable(String.class));
   }

   @Test
   public void testMarkMarshallableUpdateExisting() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      // First get predictor (creates entry with null isMarshallable)
      hints.getBufferSizePredictor(Integer.class);
      assertFalse(hints.isKnownMarshallable(Integer.class));

      // Now mark as marshallable - should update existing entry
      hints.markMarshallable(Integer.class, true);
      assertTrue(hints.isKnownMarshallable(Integer.class));
      assertTrue(hints.isMarshallable(Integer.class));
   }

   @Test
   public void testMarkMarshallableFlipValue() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, true);
      assertTrue(hints.isMarshallable(String.class));

      hints.markMarshallable(String.class, false);
      assertFalse(hints.isMarshallable(String.class));
   }

   @Test
   public void testClear() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, true);
      hints.markMarshallable(Integer.class, false);
      assertTrue(hints.isKnownMarshallable(String.class));

      hints.clear();
      assertFalse(hints.isKnownMarshallable(String.class));
      assertFalse(hints.isKnownMarshallable(Integer.class));
   }

   @Test
   public void testDifferentClassesIndependent() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      hints.markMarshallable(String.class, true);
      hints.markMarshallable(Integer.class, false);

      assertTrue(hints.isMarshallable(String.class));
      assertFalse(hints.isMarshallable(Integer.class));
   }

   @Test
   public void testNullBufferSizePredictorRecordSize() {
      MarshallableTypeHints hints = new MarshallableTypeHints();
      BufferSizePredictor predictor = hints.getBufferSizePredictor((Object) null);
      // recordSize should be a no-op
      predictor.recordSize(100);
      // nextSize should still return 1
      assertTrue(predictor.nextSize(null) == 1);
   }
}
