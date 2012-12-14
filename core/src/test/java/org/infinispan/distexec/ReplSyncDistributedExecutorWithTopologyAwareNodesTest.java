package org.infinispan.distexec;

import org.infinispan.configuration.cache.CacheMode;

/**
 * Tests for verifying Distributed Executors for REPL_SYNC cache mode, with Topology Aware nodes.
 *
 * @author Anna Manukyan
 */
public class ReplSyncDistributedExecutorWithTopologyAwareNodesTest extends DistributedExecutorWithTopologyAwareNodesTest {

   public CacheMode getCacheMode() {
      return CacheMode.REPL_SYNC;
   }
}
