package org.infinispan.client.hotrod.query;

import static org.infinispan.client.hotrod.test.HotRodClientTestingUtil.registerSCI;
import static org.infinispan.configuration.cache.IndexStorage.LOCAL_HEAP;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.test.HotRodClientTestingUtil;
import org.infinispan.client.hotrod.test.SingleHotRodServerTest;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoName;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

/**
 * Tests for remote queries over HotRod using protostream annotations on a local cache using indexing in RAM.
 *
 * @author Adrian Nistor
 */
@Test(testName = "client.hotrod.query.RemoteQueryWithProtostreamAnnotationsTest", groups = "functional")
public class RemoteQueryWithProtostreamAnnotationsTest extends SingleHotRodServerTest {

   @Indexed
   @ProtoName("Memo")
   public static class Memo {

      private int id;

      private String text;

      private Author author;

      public Memo(int id, String text) {
         this.id = id;
         this.text = text;
      }

      public Memo() {
      }

      @ProtoField(number = 10, defaultValue = "0")
      public int getId() {
         return id;
      }

      public void setId(int id) {
         this.id = id;
      }

      @Basic(projectable = true)
      @ProtoField(20)
      public String getText() {
         return text;
      }

      public void setText(String text) {
         this.text = text;
      }

      @Basic(projectable = true)
      @ProtoField(30)
      public Author getAuthor() {
         return author;
      }

      public void setAuthor(Author author) {
         this.author = author;
      }

      @Override
      public String toString() {
         return "Memo{id=" + id + ", text='" + text + '\'' + ", author=" + author + '}';
      }
   }

   @Indexed
   @ProtoName("Author")
   public static class Author {

      private int id;

      private String name;

      public Author(int id, String name) {
         this.id = id;
         this.name = name;
      }

      public Author() {
      }

      @ProtoField(number = 1, defaultValue = "0")
      public int getId() {
         return id;
      }

      public void setId(int id) {
         this.id = id;
      }

      @Basic(projectable = true)
      @ProtoField(number = 2)
      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return "Author{id=" + id + ", name='" + name + "'}";
      }
   }

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      org.infinispan.configuration.cache.ConfigurationBuilder builder = new org.infinispan.configuration.cache.ConfigurationBuilder();
      builder.indexing().enable()
            .storage(LOCAL_HEAP)
            .addIndexedEntity("Memo");

      EmbeddedCacheManager manager = TestCacheManagerFactory.createServerModeCacheManager();

      manager.defineConfiguration("test", builder.build());

      return manager;
   }

   @Override
   protected RemoteCacheManager getRemoteCacheManager() {
      org.infinispan.client.hotrod.configuration.ConfigurationBuilder clientBuilder = HotRodClientTestingUtil.newRemoteConfigurationBuilder();
      clientBuilder.addServer().host("127.0.0.1").port(hotrodServer.getPort());
      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(clientBuilder.build());
      registerSCI(remoteCacheManager, RemoteQueryWithProtostreamAnnotationsTestSCI.INSTANCE);
      return remoteCacheManager;
   }

   public void testAttributeQuery() {
      RemoteCache<Integer, Memo> remoteCache = remoteCacheManager.getCache("test");

      remoteCache.put(1, createMemo1());
      remoteCache.put(2, createMemo2());

      // get memo1 back from remote cache and check its attributes
      Memo fromCache = remoteCache.get(1);
      assertMemo1(fromCache);

      // get memo1 back from remote cache via query and check its attributes
      QueryFactory qf = Search.getQueryFactory(remoteCache);
      Query<Memo> query = qf.from(Memo.class)
            .having("text").like("%ipsum%")
            .build();
      List<Memo> list = query.execute().list();
      assertNotNull(list);
      assertEquals(1, list.size());
      assertEquals(Memo.class, list.get(0).getClass());
      assertMemo1(list.get(0));

      // get memo2 back from remote cache via query and check its attributes
      query = qf.from(Memo.class)
            .having("author.name").eq("Adrian")
            .build();
      list = query.execute().list();
      assertNotNull(list);
      assertEquals(1, list.size());
      assertEquals(Memo.class, list.get(0).getClass());
      assertMemo2(list.get(0));
   }

   private Memo createMemo1() {
      Memo memo = new Memo(1, "Lorem ipsum");
      memo.setAuthor(new Author(1, "Tom"));
      return memo;
   }

   private Memo createMemo2() {
      Memo memo = new Memo(2, "Sed ut perspiciatis unde omnis iste natus error");
      memo.setAuthor(new Author(2, "Adrian"));
      return memo;
   }

   private void assertMemo1(Memo memo) {
      assertNotNull(memo);
      assertEquals(1, memo.getId());
      assertEquals("Lorem ipsum", memo.getText());
      assertEquals(1, memo.getAuthor().getId());
   }

   private void assertMemo2(Memo memo) {
      assertNotNull(memo);
      assertEquals(2, memo.getId());
      assertEquals("Sed ut perspiciatis unde omnis iste natus error", memo.getText());
      assertEquals(2, memo.getAuthor().getId());
   }

   @ProtoSchema(
         includeClasses = {Author.class, Memo.class},
         schemaFileName = "test.client.RemoteQueryWithProtostreamAnnotationsTest.proto",
         schemaFilePath = "proto/generated",
         service = false
   )
   public interface RemoteQueryWithProtostreamAnnotationsTestSCI extends GeneratedSchema {
      GeneratedSchema INSTANCE = new RemoteQueryWithProtostreamAnnotationsTestSCIImpl();
   }
}
