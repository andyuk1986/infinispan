package org.infinispan.client.hotrod.query.testdomain.protobuf;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Decimal;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.protostream.types.java.CommonTypes;

@Indexed
public class CalculusIndexed {

   private final String name;

   private final BigInteger purchases;

   private final BigDecimal prospect;

   private final BigDecimal decimal;

   @ProtoFactory
   public CalculusIndexed(String name, BigInteger purchases, BigDecimal prospect, BigDecimal decimal) {
      this.name = name;
      this.purchases = purchases;
      this.prospect = prospect;
      this.decimal = decimal;
   }

   @Keyword
   @ProtoField(value = 1)
   public String getName() {
      return name;
   }

   @Basic
   @ProtoField(value = 2)
   public BigInteger getPurchases() {
      return purchases;
   }

   @Basic // the scale is 0 by default
   @ProtoField(value = 3)
   public BigDecimal getProspect() {
      return prospect;
   }

   @Decimal // the scale is 2 by default
   @ProtoField(value = 4)
   public BigDecimal getDecimal() {
      return decimal;
   }

   @ProtoSchema(
         includeClasses = CalculusIndexed.class,
         dependsOn = CommonTypes.class,
         schemaFilePath = "/protostream",
         schemaFileName = "calculus-indexed.proto",
         schemaPackageName = "lab.indexed",
         service = false
   )
   public interface CalculusIndexedSchema extends GeneratedSchema {
   }
}
