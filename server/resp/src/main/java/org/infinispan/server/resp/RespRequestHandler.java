package org.infinispan.server.resp;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.server.resp.commands.connection.QUIT;
import org.infinispan.server.resp.serialization.Resp3Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public abstract class RespRequestHandler {
   protected final CompletionStage<RespRequestHandler> myStage = CompletableFuture.completedFuture(this);
   protected final RespServer respServer;

   public static final AttributeKey<ByteBufPool> BYTE_BUF_POOL_ATTRIBUTE_KEY = AttributeKey.newInstance("buffer-pool");
   private final BiFunction<RespRequestHandler, Throwable, RespRequestHandler> HANDLE_FAILURE = (h, t) -> {
      if (t != null) {
         Resp3Response.error(t, allocator());
         // If exception, never use handler
         return this;
      }
      return h;
   };

   ByteBufPool allocatorToUse;
   ChannelHandlerContext ctx;

   protected RespRequestHandler(RespServer server) {
      this.respServer = server;
   }

   protected void initializeIfNecessary(ChannelHandlerContext ctx) {
      if (allocatorToUse == null) {
         if (!ctx.channel().hasAttr(BYTE_BUF_POOL_ATTRIBUTE_KEY)) {
            throw new IllegalStateException("BufferPool was not initialized in the context " + ctx);
         }
         allocatorToUse = ctx.channel().attr(BYTE_BUF_POOL_ATTRIBUTE_KEY).get();
         this.ctx = ctx;
      }
   }

   public RespServer respServer() {
      return respServer;
   }

   public CompletionStage<RespRequestHandler> myStage() {
      return myStage;
   }

   /**
    * Acquire the buffer allocator in the current context.
    * <p>
    * The {@link ByteBufPool} provides the means to allocate a {@link io.netty.buffer.ByteBuf} with the required
    * free size. <b>All</b> the writes must utilize the {@link ByteBufPool} instead of manually allocating buffers.
    * </p>
    * <b>Thread-safety</b>: The allocator and the {@link io.netty.buffer.ByteBuf} must be utilized only from the
    * event loop thread. Use the {@link ChannelHandlerContext} to verify.
    *
    * @return A {@link io.netty.buffer.ByteBuf} allocator.
    */
   public ByteBufPool allocator() {
      assert ctx.channel().eventLoop().inEventLoop() : "Buffer allocation should occur in event loop, it was " + Thread.currentThread().getName();
      return allocatorToUse;
   }

   public final CompletionStage<RespRequestHandler> handleRequest(ChannelHandlerContext ctx, RespCommand command, List<byte[]> arguments) {
      initializeIfNecessary(ctx);
      if (command == null) {
         RespErrorUtil.unknownCommand(allocatorToUse);
         return myStage;
      }

      if (!command.hasValidNumberOfArguments(arguments)) {
         RespErrorUtil.wrongArgumentNumber(command, allocator());
         return myStage;
      }

      try {
         return actualHandleRequest(ctx, command, arguments);
      } catch (Exception e) {
         // Extract the handler capable of writing the error message.
         Consumer<ByteBufPool> writer = RespErrorUtil.handleException(e);

         // If there is no writer capable of handling the message, return the exception to the lower level to handle.
         if (writer == null) {
            return CompletableFuture.failedFuture(e);
         }

         writer.accept(allocatorToUse);
         return myStage();
      }
   }

   /**
    * Handles the RESP request returning a stage that when complete notifies the command has completed as well as
    * providing the request handler for subsequent commands.
    * <p>
    * Implementations should never use the ByteBufAllocator in the context.
    * Instead, they should use {@link #allocatorToUse} to retrieve a ByteBuffer.
    * This ByteBuffer should only have bytes written to it adding up to the size requested.
    * The ByteBuffer itself should never be written to the context or channel and flush should also never be invoked.
    * Failure to do so may cause mis-ordering or responses as requests support pipelining and a ByteBuf may not be
    * send down stream until later in the pipeline.
    *
    * @param ctx Netty context pipeline for this request
    * @param type The command type
    * @param arguments The remaining arguments to the command
    * @return stage that when complete returns the new handler to instate. This stage <b>must</b> be completed on the event loop
    */
   protected CompletionStage<RespRequestHandler> actualHandleRequest(ChannelHandlerContext ctx, RespCommand type, List<byte[]> arguments) {
      if (type instanceof QUIT) {
         ctx.close();
         return myStage;
      }
      RespErrorUtil.unknownCommand(allocatorToUse);
      return myStage;
   }

   public void handleChannelDisconnect(ChannelHandlerContext ctx) {
      // Do nothing by default
   }

   public  <E> CompletionStage<RespRequestHandler> stageToReturn(CompletionStage<E> stage, ChannelHandlerContext ctx,
         BiConsumer<? super E, ByteBufPool> biConsumer) {
      return stageToReturn(stage, ctx, Objects.requireNonNull(biConsumer), null);
   }

   public  <E> CompletionStage<RespRequestHandler> stageToReturn(CompletionStage<E> stage, ChannelHandlerContext ctx,
         Function<E, RespRequestHandler> handlerWhenComplete) {
      return stageToReturn(stage, ctx, null, Objects.requireNonNull(handlerWhenComplete));
   }

   public CompletionStage<RespRequestHandler> stageToReturn(CompletionStage<RespRequestHandler> stage, ChannelHandlerContext ctx) {
      assert ctx.channel().eventLoop().inEventLoop();
      if (CompletionStages.isCompletedSuccessfully(stage)) {
         RespRequestHandler rrh = CompletionStages.join(stage);
         return rrh.myStage();
      }
      return stage.handleAsync(HANDLE_FAILURE, ctx.channel().eventLoop());
   }

   /**
    * Handles ensuring that a stage TriConsumer is only invoked on the event loop. The TriConsumer can then do things
    * such as writing to the underlying channel or update variables in a thread safe manner without additional
    * synchronization or volatile variables.
    * <p>
    * If the TriConsumer provided is null the provided stage <b>must</b> be completed only on the event loop of the
    * provided ctx.
    * This can be done via the Async methods on {@link CompletionStage} such as {@link CompletionStage#thenApplyAsync(Function, Executor)};
    * <p>
    * If the returned stage was completed exceptionally, any exception is ignored, assumed to be handled in the
    * <b>biConsumer</b> or further up the stage.
    * The <b>handlerWhenComplete</b> is not invoked if the stage was completed exceptionally as well.
    * If the <b>triConsumer</b> or <b>handlerWhenComplete</b> throw an exception when invoked the returned stage will
    * complete with that exception.
    * <p>
    * This method can only be invoked on the event loop for the provided ctx.
    *
    * @param <E> The stage return value
    * @param stage The stage that will complete. Note that if biConsumer is null this stage may only be completed on the event loop
    * @param ctx The context used for this request, normally the event loop is the thing used
    * @param biConsumer The consumer to be ran on
    * @param handlerWhenComplete A function to map the result to which handler will be used for future requests. Only ever invoked on the event loop. May be null, if so the current handler is returned
    * @return A stage that is only ever completed on the event loop that provides the new handler to use
    */
   private <E> CompletionStage<RespRequestHandler> stageToReturn(CompletionStage<E> stage, ChannelHandlerContext ctx,
         BiConsumer<? super E, ByteBufPool> biConsumer, Function<E, RespRequestHandler> handlerWhenComplete) {
      assert ctx.channel().eventLoop().inEventLoop();
      // Only one or the other can be null
      assert (biConsumer != null && handlerWhenComplete == null) || (biConsumer == null && handlerWhenComplete != null) :
            "triConsumer was: " + biConsumer + " and handlerWhenComplete was: " + handlerWhenComplete;
      if (CompletionStages.isCompletedSuccessfully(stage)) {
         E result = CompletionStages.join(stage);
         try {
            if (handlerWhenComplete != null) {
               return CompletableFuture.completedFuture(handlerWhenComplete.apply(result));
            }
            biConsumer.accept(result, allocatorToUse);
         } catch (Throwable t) {
            Resp3Response.error(t, allocator());
            return myStage();
         }
         return myStage;
      }
      if (biConsumer != null) {
         // Note that this method is only ever invoked in the event loop, so this whenCompleteAsync can never complete
         // until this request completes, meaning the thenApply will always be invoked in the event loop as well
         return CompletionStages.handleAndComposeAsync(stage, (e, t) -> {
            if (t != null) {
               Resp3Response.error(t, allocator());
            } else {
               try {
                  biConsumer.accept(e, allocatorToUse);
               } catch (Throwable innerT) {
                  return CompletableFuture.failedFuture(innerT);
               }
            }
            return myStage;
         }, ctx.channel().eventLoop());
      }
      return stage.handleAsync((value, t) -> {
         if (t != null) {
            Resp3Response.error(t, allocator());
            // If exception, never use handler
            return this;
         }
         return handlerWhenComplete.apply(value);
      }, ctx.channel().eventLoop());
   }

}
