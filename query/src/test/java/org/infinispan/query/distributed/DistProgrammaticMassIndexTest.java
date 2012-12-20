/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.infinispan.query.distributed;

import junit.framework.Assert;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.queries.faceting.Car;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests verifying that the Mass Indexing for programmatic cache configuration works as well.
 */
@Test(groups = "functional", testName = "query.distributed.DistProgrammaticMassIndex")
public class DistProgrammaticMassIndexTest extends DistributedMassIndexingTest {

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder cacheCfg = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);
      cacheCfg.indexing()
            .enable()
            .indexLocalOnly(true)
            .addProperty("hibernate.search.default.indexmanager", "org.infinispan.query.indexmanager.InfinispanIndexManager")
            .addProperty("hibernate.search.default.directory_provider", "infinispan")
            .addProperty("hibernate.search.default.exclusive_index_use", "false")
            .addProperty("lucene_version", "LUCENE_36");
      List<Cache<String, Car>> cacheList = createClusteredCaches(NUM_NODES, cacheCfg);
      for(Cache cache : cacheList) {
         caches.add(cache);
      }
   }

   protected void verifyFindsCar(Cache cache, int count, String carMake) {
      /*QueryParser queryParser = createQueryParser("make");

      try {
      Query luceneQuery = queryParser.parse(carMake);
      CacheQuery cacheQuery = Search.getSearchManager(cache).getQuery(luceneQuery, Car.class);
      List<Object> found = cacheQuery.list();
         System.out.println(found.size());
      assert found.size() == count : "Size of list should be " + count + ", but is " + found.size();

      } catch(ParseException ex) {
         ex.printStackTrace();
         Assert.fail("Failed due to: " + ex.getMessage());
      }*/

      SearchManager searchManager = Search.getSearchManager(cache);
      QueryBuilder carQueryBuilder = searchManager.buildQueryBuilderForClass(Car.class).get();
      Query fullTextQuery = carQueryBuilder.keyword().onField("make").matching(carMake).createQuery();
      CacheQuery cacheQuery = searchManager.getQuery(fullTextQuery, Car.class);
      //Assert.assertEquals(count, cacheQuery.getResultSize());

      System.out.println("The query size: " + cacheQuery.getResultSize() + "cahce: " + cache);
   }

}
