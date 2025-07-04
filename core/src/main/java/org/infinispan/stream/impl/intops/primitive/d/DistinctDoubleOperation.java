package org.infinispan.stream.impl.intops.primitive.d;

import java.util.stream.DoubleStream;

import org.infinispan.commons.marshall.ProtoStreamTypeIds;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;
import org.infinispan.stream.impl.intops.IntermediateOperation;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Performs distinct operation on a {@link DoubleStream}
 */
@ProtoTypeId(ProtoStreamTypeIds.STREAM_INTOP_PRIMITIVE_DOUBLE_DISTINCT_OPERATION)
public class DistinctDoubleOperation implements IntermediateOperation<Double, DoubleStream, Double, DoubleStream> {
   private static final DistinctDoubleOperation OPERATION = new DistinctDoubleOperation();
   private DistinctDoubleOperation() { }

   @ProtoFactory
   public static DistinctDoubleOperation getInstance() {
      return OPERATION;
   }

   @Override
   public DoubleStream perform(DoubleStream stream) {
      return stream.distinct();
   }

   @Override
   public Flowable<Double> mapFlowable(Flowable<Double> input) {
      return input.distinct();
   }
}
