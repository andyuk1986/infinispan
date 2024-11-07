package org.infinispan.query.clustered.commandworkers;

import static org.infinispan.query.core.impl.Log.CONTAINER;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.query.clustered.QueryResponse;
import org.infinispan.query.dsl.embedded.impl.SearchQueryBuilder;

/**
 * Deletes the matching results on current node.
 *
 * @author anistor@redhat.com
 * @since 13.0
 */
final class CQDelete extends CQWorker {

   @Override
   CompletionStage<QueryResponse> perform(BitSet segments) {
      setFilter(segments);

      // Must never apply any kind of limits to a DELETE! Limits are just for paging a SELECT.
      if (queryDefinition.getFirstResult() != 0 || queryDefinition.isCustomMaxResults()) {
         throw CONTAINER.deleteStatementsCannotUsePaging();
      }

      SearchQueryBuilder query = queryDefinition.getSearchQueryBuilder();
      return blockingManager.supplyBlocking(() -> fetchIds(query), this)
            .thenCompose(queryResult -> CompletionStages.performSequentially(queryResult.iterator(), cache::removeAsync,
                  Collectors.summingInt(prev -> prev != null ? 1 : 0)))
            .thenApply(QueryResponse::new);
   }

   public List<Object> fetchIds(SearchQueryBuilder query) {
      long start = queryStatistics.isEnabled() ? System.nanoTime() : 0;
      List<Object> result = query.ids().fetchAllHits();
      if (queryStatistics.isEnabled()) {
         queryStatistics.localIndexedQueryExecuted(queryDefinition.getQueryString(), System.nanoTime() - start);
      }
      return result;
   }
}
