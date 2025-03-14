package org.infinispan.client.hotrod.evolution.model;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.client.hotrod.annotation.model.Model;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoName;
import org.infinispan.protostream.annotations.ProtoReserved;
import org.infinispan.protostream.annotations.ProtoSchema;

@Indexed
@ProtoName("Model") // F
@ProtoReserved(numbers = 3, names = "name")
public class BaseModelWithNameIndexedFieldEntity implements Model {

   @ProtoField(number = 1)
   @Basic(projectable = true)
   public Integer entityVersion;

   @ProtoField(number = 2)
   public String id;

   @ProtoField(number = 4)
   @Text
   public String analyzed;

   @Override
   public String getId() {
      return id;
   }

   @ProtoSchema(includeClasses = BaseModelWithNameIndexedFieldEntity.class, schemaFileName = "evolution-schema.proto", schemaPackageName = "evolution", service = false)
   public interface BaseModelWithNameIndexedFieldEntitySchema extends GeneratedSchema {
      BaseModelWithNameIndexedFieldEntitySchema INSTANCE = new BaseModelWithNameIndexedFieldEntitySchemaImpl();
   }
}
