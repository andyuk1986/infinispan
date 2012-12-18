package org.infinispan.query.blackbox;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.test.Person;
import org.testng.annotations.Test;

import java.util.List;

import static org.infinispan.query.helper.TestQueryHelperFactory.createQueryParser;

/**
 * Test is added for verifying the query for Clustered cache with DIST_SYNC cache mode.
 *
 * @author Anna Manukyan
 */
@Test(groups = "functional", testName = "query.blackbox.DistClusteredCacheTest")
public class DistClusteredCacheTest extends ClusteredCacheTest {

   public CacheMode getCacheMode() {
      return CacheMode.DIST_SYNC;
   }

   public void testAddedWithFlags() throws Exception {
      prepareTestData();
      queryParser = createQueryParser("blurb");

      luceneQuery = queryParser.parse("eats");
      cacheQuery = Search.getSearchManager(cache2).getQuery(luceneQuery);
      List<Object> found = cacheQuery.list();

      assert found.size() == 2 : "Size of list should be 2";
      assert found.contains(person2);
      assert found.contains(person3);

      person4 = new Person();
      person4.setName("Mighty Goat");
      person4.setBlurb("Also eats grass");

      cache1.getAdvancedCache().withFlags(Flag.SKIP_INDEXING).put("mighty", person4);

      luceneQuery = queryParser.parse("eats");
      cacheQuery = Search.getSearchManager(cache2).getQuery(luceneQuery);
      found = cacheQuery.list();

      assert found.size() == 2 : "Size of list should be 2";
      assert found.contains(person2);
      assert found.contains(person3);
      assert !found.contains(person4) : "This should now contain object person4";

      SearchManager searchManager = Search.getSearchManager(cache2);
      searchManager.getMassIndexer().start();


      luceneQuery = queryParser.parse("eats");
      cacheQuery = Search.getSearchManager(cache2).getQuery(luceneQuery);
      found = cacheQuery.list();

      assert found.size() == 3 : "Size of list should be 3, but is " + found.size();
      assert found.contains(person2);
      assert found.contains(person3);
      assert found.contains(person4) : "This should now contain object person4";
   }

}
