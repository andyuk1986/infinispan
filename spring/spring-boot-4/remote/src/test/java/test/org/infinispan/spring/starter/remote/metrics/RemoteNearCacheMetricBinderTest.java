package test.org.infinispan.spring.starter.remote.metrics;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.server.test.api.TestUser;
import org.infinispan.server.test.junit5.InfinispanServerExtension;
import org.infinispan.server.test.junit5.InfinispanServerExtensionBuilder;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RemoteNearCacheMetricBinderTest extends RemoteCacheMetricBinderTest{

   @RegisterExtension
   static InfinispanServerExtension infinispanServerExtension = InfinispanServerExtensionBuilder.server("mycache.xml");

   @Override
   public RemoteCache<String, String> createCache() {
      org.infinispan.configuration.cache.ConfigurationBuilder cacheConfigBuilder =
            new org.infinispan.configuration.cache.ConfigurationBuilder();
      cacheConfigBuilder.clustering().cacheMode(CacheMode.DIST_SYNC);

      ConfigurationBuilder clientBuilder = new ConfigurationBuilder();
      clientBuilder.remoteCache("mycache").nearCacheMode(NearCacheMode.INVALIDATED).nearCacheMaxEntries(2);
      clientBuilder.statistics().enable();
      clientBuilder.clientIntelligence(ClientIntelligence.BASIC);
      clientBuilder.security()
            .authentication()
            .username(TestUser.ADMIN.getUser())
            .password(TestUser.ADMIN.getPassword());

      RemoteCacheManager remoteCacheManager = infinispanServerExtension.hotrod().withClientConfiguration(clientBuilder)
            .createRemoteCacheManager();
      setRemoteCacheManager(remoteCacheManager);
      return remoteCacheManager.getCache("mycache");
   }

}
