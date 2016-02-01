package org.infinispan.scripting;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.ExecutionException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.infinispan.commons.CacheException;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.tasks.TaskContext;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "scripting.ScriptingTest")
public class ScriptingTest extends SingleCacheManagerTest {

   protected ScriptingManager scriptingManager;

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      return TestCacheManagerFactory.createCacheManager();
   }

   protected String[] getScripts() {
      return new String[] { "test.js", "testMissingMetaProps.js", "testExecWithoutProp.js" };
   }

   @Override
   protected void setup() throws Exception {
      super.setup();
      scriptingManager = cacheManager.getGlobalComponentRegistry().getComponent(ScriptingManager.class);
      for (String scriptName : getScripts()) {
         try (InputStream is = this.getClass().getResourceAsStream("/" + scriptName)) {
            String script = TestingUtil.loadFileAsString(is);
            scriptingManager.addScript(scriptName, script);
         }
      }
   }

   @Override
   protected void clearContent() {
      cacheManager.getCache().clear();
   }

   public void testSimpleScript() throws Exception {
      String result = (String) scriptingManager.runScript("test.js", new TaskContext().addParameter("a", "a")).get();
      assertEquals("a", result);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026005.*")
   public void testRunNonExistentScript() throws Exception {
      String result = (String) scriptingManager.runScript("nonExistent.js", new TaskContext().addParameter("a", "a")).get();
      assertEquals("a", result);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026003.*")
   public void testSimpleScriptWitoutPassingParameter() throws Exception {
      String result = (String) scriptingManager.runScript("test.js").get();
      assertEquals("a", result);
   }

   public void testSimpleScrip1() throws Exception {
      ScriptObjectMirror result = (ScriptObjectMirror) scriptingManager.runScript("testExecWithoutProp.js").get();

      long resultTimeInMillis = (Long) result.callMember("getTime");

      LocalDateTime currentDate = LocalDateTime.now();
      LocalDateTime expectedResult = currentDate.minus(Period.ofDays(5));

      //assertEquals(expectedResult.toEpochSecond(Zone))
   }

   public void testSimpleScriptWithMissingLanguageInMetaPropeties() throws Exception {
      String result = (String) scriptingManager.runScript("testMissingMetaProps.js", new TaskContext().addParameter("a", "a")).get();
      assertEquals("a", result);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026005.*")
   public void testRemovingNonExistentScript() {
      scriptingManager.removeScript("nonExistent");
   }

   public void testRemovingScript() throws IOException, ExecutionException, InterruptedException {
      assertNotNull(cacheManager.getCache(ScriptingManager.SCRIPT_CACHE).get("test.js"));

      scriptingManager.removeScript("test.js");
      assertNull(cacheManager.getCache(ScriptingManager.SCRIPT_CACHE).get("test.js"));

      InputStream is = this.getClass().getResourceAsStream("/test.js");
      String script = TestingUtil.loadFileAsString(is);

      scriptingManager.addScript("test.js", script);
      assertNotNull(cacheManager.getCache(ScriptingManager.SCRIPT_CACHE).get("test.js"));
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026003.*")
   public void testWrongJavaRef() throws Exception {
      InputStream is = this.getClass().getResourceAsStream("/testWrongJavaRef.js");
      String script = TestingUtil.loadFileAsString(is);

      scriptingManager.addScript("testWrongJavaRef.js", script);

      String result = (String) scriptingManager.runScript("testWrongJavaRef.js", new TaskContext().addParameter("a", "a")).get();
      assertEquals("a", result);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026003.*")
   public void testWrongPropertyRef() throws Exception {
      InputStream is = this.getClass().getResourceAsStream("/testWrongPropertyRef.js");
      String script = TestingUtil.loadFileAsString(is);

      scriptingManager.addScript("testWrongPropertyRef.js", script);

      String result = (String) scriptingManager.runScript("testWrongPropertyRef.js").get();
      assertEquals("a", result);
   }

   @Test(expectedExceptions = CacheException.class, expectedExceptionsMessageRegExp = "^ISPN026004.*")
   public void testJsCompilationError() throws Exception {
      InputStream is = this.getClass().getResourceAsStream("/testJsCompilationError.js");
      String script = TestingUtil.loadFileAsString(is);

      scriptingManager.addScript("testJsCompilationError.js", script);

      String result = (String) scriptingManager.runScript("testJsCompilationError.js").get();
      assertEquals("a", result);
   }
}
