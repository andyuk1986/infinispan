package org.infinispan.query.config;

import org.infinispan.config.Configuration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.LegacyConfigurationAdaptor;
import org.infinispan.util.TypedProperties;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Tests are added testing the LegacyConfigurationAdaptor for Indexing options.
 *
 * @author Anna Manukyan
 */
@Test(groups = "functional", testName = "query.config.LegacyConfigurationAdaptorTests")
public class LegacyConfigurationAdaptorTests {

   @Test
   public void testIndexingWithLegacyAdapt() {
      ConfigurationBuilder cacheCfg = new ConfigurationBuilder();
      cacheCfg.indexing()
            .enable()
            .indexLocalOnly(false)
            .addProperty("default.directory_provider", "ram")
            .addProperty("lucene_version", "LUCENE_CURRENT");

      Configuration legacy = LegacyConfigurationAdaptor.adapt(cacheCfg.build());

      assert legacy.isIndexingEnabled();
      assert !legacy.isIndexLocalOnly();
      TypedProperties p = legacy.getIndexingProperties();

      AssertJUnit.assertEquals("ram", p.getProperty("default.directory_provider"));
      AssertJUnit.assertEquals("LUCENE_CURRENT", p.getProperty("lucene_version"));
   }

   @Test
   public void testIndexingWithLegacyConfiguration() {
      Configuration configuration = new Configuration();
      configuration.setIndexingEnabled(true);
      configuration.setIndexLocalOnly(true);
      configuration.getIndexingProperties().put("default.directory_provider", "ram");
      configuration.getIndexingProperties().put("lucene_version", "LUCENE_CURRENT");

      org.infinispan.configuration.cache.Configuration legacy = LegacyConfigurationAdaptor.adapt(configuration);

      assert legacy.indexing().enabled();
      assert legacy.indexing().indexLocalOnly();
      TypedProperties p = legacy.indexing().properties();

      AssertJUnit.assertEquals("ram", p.getProperty("default.directory_provider"));
      AssertJUnit.assertEquals("LUCENE_CURRENT", p.getProperty("lucene_version"));
   }
}