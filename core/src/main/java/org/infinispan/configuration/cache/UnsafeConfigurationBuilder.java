package org.infinispan.configuration.cache;

import java.util.Map;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.global.GlobalConfiguration;

import static org.infinispan.configuration.cache.UnsafeConfiguration.UNRELIABLE_RETURN_VALUES;

/**
 * Controls certain tuning parameters that may break some of Infinispan's public API contracts in exchange for better
 * performance in some cases.
 * Use with care, only after thoroughly reading and understanding the documentation about a specific feature.
 */
public class UnsafeConfigurationBuilder extends AbstractConfigurationChildBuilder implements Builder<UnsafeConfiguration> {

   private final AttributeSet attributes;

   protected UnsafeConfigurationBuilder(ConfigurationBuilder builder) {
      super(builder);
      attributes = UnsafeConfiguration.attributeDefinitionSet();
   }

   @Override
   public AttributeSet attributes() {
      return attributes;
   }

   /**
    * Specify whether Infinispan is allowed to disregard the {@link Map} contract when providing return values for
    * {@link org.infinispan.Cache#put(Object, Object)} and {@link org.infinispan.Cache#remove(Object)} methods.
       * Providing return values can be expensive as they may entail a read from disk or across a network, and if the usage
    * of these methods never make use of these return values, allowing unreliable return values helps Infinispan
    * optimize away these remote calls or disk reads.
       * @param allowUnreliableReturnValues if true, return values for the methods described above should not be relied on.
    */
   public UnsafeConfigurationBuilder unreliableReturnValues(boolean allowUnreliableReturnValues) {
      attributes.attribute(UNRELIABLE_RETURN_VALUES).set(allowUnreliableReturnValues);
      return this;
   }

   @Override
   public void validate() {
      // Nothing to validate
   }

   @Override
   public void validate(GlobalConfiguration globalConfig) {
   }

   @Override
   public UnsafeConfiguration create() {
      return new UnsafeConfiguration(attributes.protect());
   }

   @Override
   public UnsafeConfigurationBuilder read(UnsafeConfiguration template, Combine combine) {
      this.attributes.read(template.attributes(), combine);
      return this;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + attributes;
   }
}
