package org.infinispan.scripting;

import org.infinispan.commons.CacheException;
import org.infinispan.tasks.TaskContext;
import org.infinispan.test.TestingUtil;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

@Test(groups = "functional", testName = "scripting.ScriptingTest")
public class ScriptingTest extends AbstractScriptingTest {

   protected String[] getScripts() {
      return new String[] { "test.js", "testMissingMetaProps.js", "testExecWithoutProp.js", "testInnerScriptCall.js" };
   }

   @Override
   protected void setup() throws Exception {
      super.setup();
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

   public void testSimpleScript1() throws Exception {
      String value = "javaValue";
      String key = "processValue";

      cacheManager.getCache("test_cache").put(key, value);

      CompletableFuture exec = scriptingManager.runScript("testExecWithoutProp.js");
      while (!exec.isDone()) {
         Thread.sleep(1000);
      }

      assertEquals(value + ":additionFromJavascript", cacheManager.getCache("test_cache").get(key));
   }

   public void testScriptCallFromJavascript() throws Exception {
      assertNull(cacheManager.getCache().get("a"));

      String result = (String) scriptingManager.runScript("testInnerScriptCall.js",
              new TaskContext().cache(cacheManager.getCache("test_cache")).addParameter("a", "ahoj")).get();

      assertEquals("script1:additionFromJavascript", result);
      assertEquals("ahoj", cacheManager.getCache().get("a"));
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
