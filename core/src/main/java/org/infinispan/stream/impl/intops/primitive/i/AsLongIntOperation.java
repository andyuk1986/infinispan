package org.infinispan.stream.impl.intops.primitive.i;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.infinispan.commons.marshall.ProtoStreamTypeIds;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;
import org.infinispan.stream.impl.intops.MappingOperation;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Performs as long operation on a {@link IntStream}
 */
@ProtoTypeId(ProtoStreamTypeIds.STREAM_INTOP_PRIMITIVE_INT_AS_LONG_OPERATION)
public class AsLongIntOperation implements MappingOperation<Integer, IntStream, Long, LongStream> {
   private static final AsLongIntOperation OPERATION = new AsLongIntOperation();
   private AsLongIntOperation() { }

   @ProtoFactory
   public static AsLongIntOperation getInstance() {
      return OPERATION;
   }

   @Override
   public LongStream perform(IntStream stream) {
      return stream.asLongStream();
   }

   @Override
   public Flowable<Long> mapFlowable(Flowable<Integer> input) {
      return input.map(Integer::longValue);
   }
}
