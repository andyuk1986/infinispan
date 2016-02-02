package org.infinispan.scripting;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.stream.CacheCollectors;
import org.infinispan.tasks.TaskContext;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.data.Address;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.infinispan.scripting.utils.ScriptingUtils.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Testing the Scripting on distributed cluster.
 */
@Test(groups = "functional", testName = "scripting.DistributedCacheScriptingTest")
public class DistributedCacheScriptingTest extends MultipleCacheManagersTest {

    @Override
    protected void createCacheManagers() throws Throwable {
        final ConfigurationBuilder conf = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);
        createCluster(conf, 4);
        waitForClusterToForm();
    }

    public void testDistributedScriptExecution() throws IOException, ExecutionException, InterruptedException {
        ScriptingManager scriptingManager = getScriptingManager(manager(0));
        loadScript(scriptingManager, "/distExec.js");

        List<Address> addressList = (List<Address>) scriptingManager.runScript("distExec.js", new TaskContext().cache(cache(0))).get();
        assertTrue(addressList.contains(manager(0).getAddress()));
        assertTrue(addressList.contains(manager(1).getAddress()));
        assertTrue(addressList.contains(manager(2).getAddress()));
        assertTrue(addressList.contains(manager(3).getAddress()));
    }

    public void testDistributedMapReduceStream() throws IOException, ExecutionException, InterruptedException {
        ScriptingManager scriptingManager = getScriptingManager(manager(0));
        Cache cache = cache(0);

        loadData(cache, "/macbeth.txt");
        loadScript(scriptingManager, "/wordCountStream_dist.js");

        /*CompletableFuture<Map<String, Long>> resultsFuture = scriptingManager.runScript("wordCountStream_dist.js", new TaskContext().cache(cache(0)));
        Map<String, Long> results = resultsFuture.get();*/

        Map<String, Integer> results = (Map<String, Integer>) cache.entrySet().stream()
                .map((Serializable & Function<Map.Entry<String, String>, String[]>) e -> e.getValue().split("[\\W]+"))
                .flatMap((Serializable & Function<String[], Stream<String>>) Arrays::stream)
                .collect(CacheCollectors.serializableCollector(
                        () -> Collectors.groupingBy(Function.identity(), Collectors.counting())));
//        assertEquals(3209, results.size());
//        assertEquals(results.get("macbeth"), Long.valueOf(287));
    }
}
