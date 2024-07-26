package org.infinispan.server.resp.commands.string;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.infinispan.server.resp.Resp3Handler;
import org.infinispan.server.resp.RespCommand;
import org.infinispan.server.resp.RespRequestHandler;
import org.infinispan.server.resp.commands.Resp3Command;
import org.infinispan.server.resp.serialization.Resp3Response;

import io.netty.channel.ChannelHandlerContext;

/**
 * @link https://redis.io/commands/incr/
 * @since 14.0
 */
public class INCR extends RespCommand implements Resp3Command {
   public INCR() {
      super(2, 1, 1, 1);
   }

   @Override
   public CompletionStage<RespRequestHandler> perform(Resp3Handler handler,
                                                      ChannelHandlerContext ctx,
                                                      List<byte[]> arguments) {
      return handler
            .stageToReturn(CounterIncOrDec.counterIncOrDec(handler.cache(), arguments.get(0), true),
                  ctx, Resp3Response.INTEGER);
   }
}
