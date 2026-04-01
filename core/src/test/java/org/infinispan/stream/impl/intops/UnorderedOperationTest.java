package org.infinispan.stream.impl.intops;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.Test;

/**
 * Tests for {@link UnorderedOperation}.
 */
@Test(groups = "unit", testName = "stream.impl.intops.UnorderedOperationTest")
public class UnorderedOperationTest {

   public void testPerformRemovesOrdering() {
      UnorderedOperation<String, Stream<String>> op = new UnorderedOperation<>();
      Stream<String> result = (Stream<String>) op.perform(Stream.of("a", "b", "c"));
      assertFalse(result.spliterator().hasCharacteristics(Spliterator.ORDERED));
   }

   public void testMapFlowablePassthrough() {
      UnorderedOperation<Integer, Stream<Integer>> op = new UnorderedOperation<>();
      List<Integer> result = op.mapFlowable(io.reactivex.rxjava3.core.Flowable.fromIterable(
            Arrays.asList(1, 2, 3))).toList().blockingGet();
      assertEquals(Arrays.asList(1, 2, 3), result);
   }

   public void testPerformPreservesElements() {
      UnorderedOperation<String, Stream<String>> op = new UnorderedOperation<>();
      List<String> result = ((Stream<String>) op.perform(Stream.of("x", "y", "z")))
            .collect(Collectors.toList());
      assertEquals(3, result.size());
   }
}
