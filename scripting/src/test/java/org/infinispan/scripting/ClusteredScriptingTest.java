package org.infinispan.scripting;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.tasks.TaskContext;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.data.Address;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.infinispan.scripting.utils.ScriptingUtils.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@Test(groups = "functional", testName = "scripting.ClusteredScriptingTest")
public class ClusteredScriptingTest extends MultipleCacheManagersTest {

   @Override
   protected void createCacheManagers() throws Exception {
      final ConfigurationBuilder conf = getDefaultClusteredCacheConfig(CacheMode.REPL_SYNC, false);
      createCluster(conf, 2);
      waitForClusterToForm();
   }

   private void executeScriptOnManager(int num, String scriptName) throws InterruptedException, ExecutionException {
      ScriptingManager scriptingManager = getScriptingManager(manager(num));
      String s = (String) scriptingManager.runScript(scriptName, new TaskContext().addParameter("a", "a")).get();
      assertEquals("a", s);
   }

   public void testClusteredScriptExec() throws IOException, InterruptedException, ExecutionException {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      loadScript(scriptingManager, "/test.js");
      executeScriptOnManager(0, "test.js");
      executeScriptOnManager(1, "test.js");
   }

   public void testClusteredScriptStream() throws IOException, InterruptedException, ExecutionException {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      loadScript(scriptingManager, "/test.js");
      executeScriptOnManager(0, "test.js");
      executeScriptOnManager(1, "test.js");
   }

   @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "^ISPN026009.*")
   public void testDistributedScriptExecutionWithoutCacheBinding() throws IOException, ExecutionException, InterruptedException {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      loadScript(scriptingManager, "/distExec.js");

      scriptingManager.runScript("distExec.js").get();
   }

   public void testDistributedScriptExecution() throws IOException, ExecutionException, InterruptedException {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      loadScript(scriptingManager, "/distExec.js");

      List<Address> addressList = (List<Address>) scriptingManager.runScript("distExec.js", new TaskContext().cache(cache(0))).get();
      assertTrue(addressList.contains(manager(0).getAddress()));
      assertTrue(addressList.contains(manager(1).getAddress()));
   }

   public void testDistExecScript() throws InterruptedException, ExecutionException, IOException {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      Cache<String, String> cache = cache(0);
      loadData(cache, "/macbeth.txt");
      loadScript(scriptingManager, "/wordCountStream.js");
      CompletableFuture<Map<String, Long>> resultsFuture = scriptingManager.runScript("wordCountStream.js", new TaskContext().cache(cache(0)));
      Map<String, Long> results = resultsFuture.get();
      assertEquals(3209, results.size());
      assertEquals(results.get("macbeth"), Long.valueOf(287));
   }

   public void testMapReduce() throws Exception {
      ScriptingManager scriptingManager = getScriptingManager(manager(0));
      Cache<String, String> cache = cache(0);
      loadData(cache, "/macbeth.txt");
      loadScript(scriptingManager, "/wordCountMapper.js");
      loadScript(scriptingManager, "/wordCountReducer.js");
      loadScript(scriptingManager, "/wordCountCollator.js");
      CompletableFuture<Object> future = scriptingManager.runScript("wordCountMapper.js", new TaskContext().cache(cache));
      LinkedHashMap<String, Double> results = (LinkedHashMap<String, Double>)future.get();
      assertEquals(20, results.size());
      assertTrue(results.get("macbeth").equals(Double.valueOf(287)));
   }



}
