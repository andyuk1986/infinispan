package org.infinispan.server.hotrod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.infinispan.commons.logging.LogFactory;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.security.actions.SecurityActions;
import org.infinispan.server.core.ServerConstants;
import org.infinispan.server.core.logging.Log;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.telemetry.InfinispanTelemetry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

abstract class BaseDecoder extends ByteToMessageDecoder {
   protected static final Log log = LogFactory.getLog(BaseDecoder.class, Log.class);

   protected final EmbeddedCacheManager cacheManager;
   protected final Executor executor;
   protected final HotRodServer server;
   protected final int maxContentLength;
   // And this is the ByteBuf pos before decode is performed
   protected int posBefore;

   protected Authentication auth;
   protected TransactionRequestProcessor cacheProcessor;
   protected CounterRequestProcessor counterProcessor;
   protected MultimapRequestProcessor multimapProcessor;
   protected TaskRequestProcessor taskProcessor;

   protected BaseDecoder(EmbeddedCacheManager cacheManager, Executor executor, HotRodServer server) {
      this.cacheManager = cacheManager;
      this.executor = executor;
      this.server = server;
      HotRodServerConfiguration configuration = server.getConfiguration();
      this.maxContentLength = configuration.maxContentLengthBytes();
   }

   public Executor getExecutor() {
      return executor;
   }

   protected String checkCacheReady(String cacheName) {
      if (cacheName != null && cacheManager.cacheExists(cacheName) && !cacheManager.isRunning(cacheName))
         throw log.cacheIsNotReady(cacheName);

      return cacheName;
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) {
      InfinispanTelemetry telemetryService = SecurityActions.getGlobalComponentRegistry(cacheManager)
            .getComponent(InfinispanTelemetry.class);

      auth = new Authentication(ctx.channel(), executor, server);
      cacheProcessor = new TransactionRequestProcessor(ctx.channel(), executor, server, telemetryService);
      counterProcessor = new CounterRequestProcessor(ctx.channel(), executor, server);
      multimapProcessor = new MultimapRequestProcessor(ctx.channel(), executor, server);
      taskProcessor = new TaskRequestProcessor(ctx.channel(), executor, server);
   }

   @Override
   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      super.channelActive(ctx);
      if (log.isTraceEnabled()) {
         log.tracef("Channel %s became active", ctx.channel());
      }
      server.getClientListenerRegistry().findAndWriteEvents(ctx.channel());
      server.getClientCounterNotificationManager().channelActive(ctx.channel());
   }

   @Override
   public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
      super.channelWritabilityChanged(ctx);
      Channel channel = ctx.channel();
      boolean writeable = channel.isWritable();
      if (log.isTraceEnabled()) {
         log.tracef("Channel %s writability changed to %b", channel, writeable);
      }
      if (writeable) {
         server.getClientListenerRegistry().findAndWriteEvents(channel);
         server.getClientCounterNotificationManager().channelActive(channel);
      }
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
      if (log.isTraceEnabled()) log.trace("Exception caught", t);
      if (t instanceof DecoderException) {
         t = t.getCause();
      }
      if (!ctx.channel().isActive() && t instanceof IllegalStateException &&
          t.getMessage().equals("ssl is null")) {
         // Workaround for ISPN-12996 -- OpenSSLEngine shut itself down too soon
         // Ignore the exception, trying to write a response or close the context will cause a StackOverflowError
         return;
      }

      cacheProcessor.writeException(getHeader(), t);
   }

   protected abstract HotRodHeader getHeader();

   protected int defaultExpiration(int duration, int flags, ProtocolFlag defaultFlag) {
      if (duration > 0) return duration;
      return (flags & defaultFlag.getValue()) != 0 ? ServerConstants.EXPIRATION_DEFAULT : ServerConstants.EXPIRATION_NONE;
   }

   /**
    * We usually know the size of the map ahead, and we want to return static empty map if we're not going to add anything.
    */
   protected <K, V> Map<K, V> allocMap(int size) {
      return size == 0 ? Collections.emptyMap() : new HashMap<>(size * 4/3, 0.75f);
   }

   protected <T> List<T> allocList(int size) {
      return size == 0 ? Collections.emptyList() : new ArrayList<>(size);
   }

   protected <T> Set<T> allocSet(int size) {
      return size == 0 ? Collections.emptySet() : new HashSet<>(size);
   }
}
