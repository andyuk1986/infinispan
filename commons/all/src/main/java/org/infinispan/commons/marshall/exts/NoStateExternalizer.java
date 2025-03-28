package org.infinispan.commons.marshall.exts;

import java.io.IOException;
import java.io.ObjectOutput;

import org.infinispan.commons.marshall.AbstractExternalizer;

/**
 * An externalizer that writes no state. It simply marshalls class information.
 *
 * @author Galder Zamarreño
 * @since 5.0
 * @param <T> the type
 */
public abstract class NoStateExternalizer<T> extends AbstractExternalizer<T> {

   @Override
   public void writeObject(ObjectOutput output, T object) throws IOException {
      // The instance has no state, so no-op.
   }

}
