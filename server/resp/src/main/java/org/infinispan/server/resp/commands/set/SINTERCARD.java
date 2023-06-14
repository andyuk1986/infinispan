package org.infinispan.server.resp.commands.set;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.server.resp.Consumers;
import org.infinispan.server.resp.Resp3Handler;
import org.infinispan.server.resp.RespCommand;
import org.infinispan.server.resp.RespErrorUtil;
import org.infinispan.server.resp.RespRequestHandler;
import org.infinispan.server.resp.commands.ArgumentUtils;
import org.infinispan.server.resp.commands.Resp3Command;
import org.infinispan.util.concurrent.AggregateCompletionStage;
import org.infinispan.util.concurrent.CompletionStages;

import io.netty.channel.ChannelHandlerContext;

/**
 * {@link} https://redis.io/commands/sintercard/
 *
 * Returns the cardinality of the resulting intersection set.
 *
 * When provided with the optional LIMIT argument (default 0 means unlimited),
 * if the intersection cardinality reaches limit partway through the
 * computation,
 * the algorithm exits returning LIMIT
 *
 * @since 15.0
 */
public class SINTERCARD extends RespCommand implements Resp3Command {
   static Set<WrappedByteArray> EMPTY_SET = new HashSet<>();
   static String LIMIT_OPT = "LIMIT";

   public SINTERCARD() {
      super(-3, 0, 0, 0);
   }

   @Override
   public CompletionStage<RespRequestHandler> perform(Resp3Handler handler,
         ChannelHandlerContext ctx,
         List<byte[]> arguments) {

      var keysNum = ArgumentUtils.toInt(arguments.get(0));

      final int limit = processArgs(keysNum, arguments, handler);
      if (limit < 0) { // Wrong args
         return handler.myStage();
      }
      var keys = arguments.subList(1, keysNum + 1);
      AggregateCompletionStage<Void> acs = CompletionStages.aggregateCompletionStage();
      var sets = SINTER.aggregateSets(handler, keys, acs);
      return handler.stageToReturn(acs.freeze().thenApply((v) -> (long) SINTER.intersect(sets, limit).size()),
            ctx,
            Consumers.LONG_BICONSUMER);
   }

   private int processArgs(int keysNum, List<byte[]> arguments, Resp3Handler handler) {
      // Wrong args num
      if (arguments.size() < keysNum + 1) {
         RespErrorUtil.customError("Number of keys can't be greater than number of args", handler.allocator());
         return -1;
      }
      int optVal = 0;
      if (arguments.size() > keysNum + 1) {
         if (arguments.size() != keysNum + 3) {
            // Options provided but wrong arg nums
            RespErrorUtil.syntaxError(handler.allocator());
            return -1;
         }
         var opt = new String(arguments.get(keysNum + 1)).toUpperCase();
         if (!LIMIT_OPT.equals(opt)) {
            // Wrong option provided
            RespErrorUtil.syntaxError(handler.allocator());
            return -1;
         }
         try {
            optVal = ArgumentUtils.toInt(arguments.get(keysNum + 2));
            if (optVal < 0) {
               // Negative limit provided
               RespErrorUtil.mustBePositive(handler.allocator());
               return -1;
            }
         } catch (NumberFormatException ex) {
            // Limit provided not an integer
            RespErrorUtil.valueNotInteger(handler.allocator());
            return -1;
         }
      }
      return optVal;
   }
}
