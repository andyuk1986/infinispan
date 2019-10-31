package org.infinispan.test.integration.as.query;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.infinispan.commons.util.Version;
import org.infinispan.test.integration.as.WidlflyIntegrationSCI;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.se.manifest.ManifestDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for DSL queries when using the wildfly modules
 *
 * @author gustavonalle
 * @since 7.0
 */
@RunWith(Arquillian.class)
public class DSLQueryIT {

   @Inject
   private GridService service;

   @Deployment
   @SuppressWarnings("unused")
   private static Archive<?> deployment() {
      return ShrinkWrap.create(WebArchive.class, "dsl.war")
            .addClasses(DSLQueryIT.class, QueryConfiguration.class, GridService.class)
            .addClasses(WidlflyIntegrationSCI.CLASSES)
            .add(manifest(), "META-INF/MANIFEST.MF")
            .addAsResource("dynamic-indexing-distribution.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   private static Asset manifest() {
      String manifest = Descriptors.create(ManifestDescriptor.class)
            .attribute("Dependencies", "org.infinispan:" + Version.getModuleSlot() + " services, "
                  + "org.infinispan.query:" + Version.getModuleSlot() + " services, "
                  + "org.infinispan.query.dsl:" + Version.getModuleSlot())
            .exportAsString();
      return new StringAsset(manifest);
   }

   @Test
   public void testDSLQuery() throws Exception {
      service.store("00123", new Book("Functional Programming in Scala", "manning", new Date()), true);
      List<Book> results = service.findByPublisher("manning");
      Assert.assertEquals(1, results.size());
   }
}
