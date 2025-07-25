package org.infinispan.container.entries;

import org.infinispan.commons.marshall.ProtoStreamTypeIds;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.container.versioning.SimpleClusteredVersion;
import org.infinispan.metadata.Metadata;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;
import org.infinispan.remoting.transport.Address;

/**
 * This is a metadata type used by scattered cache during state transfer. The address points to node which has last
 * known version of given entry: During key transfer RemoteMetadata is created and overwritten if another response
 * with higher version comes. During value transfer the address is already final and we request the value + metadata
 * only from this node.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
@ProtoTypeId(ProtoStreamTypeIds.REMOTE_METADATA)
public class RemoteMetadata implements Metadata {
   private final Address address;
   private final SimpleClusteredVersion version;

   public RemoteMetadata(Address address, EntryVersion version) {
      this(address, (SimpleClusteredVersion) version);
   }

   @ProtoFactory
   RemoteMetadata(Address address, SimpleClusteredVersion version) {
      this.address = address;
      this.version = version;
   }

   @ProtoField(1)
   public Address getAddress() {
      return address;
   }

   @Override
   public long lifespan() {
      return -1;
   }

   @Override
   public long maxIdle() {
      return -1;
   }

   @Override
   @ProtoField(number = 2, javaType = SimpleClusteredVersion.class)
   public EntryVersion version() {
      return version;
   }

   @Override
   public Builder builder() {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString() {
      return "RemoteMetadata{" + "address=" + address +
            ", version=" + version +
            '}';
   }
}
