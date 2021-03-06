package org.infinispan.xsite;

import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.xsite.commands.XSiteStateTransferFinishReceiveCommand;
import org.infinispan.xsite.commands.XSiteStateTransferStartReceiveCommand;
import org.infinispan.xsite.statetransfer.XSiteStatePushCommand;

/**
 * {@link org.infinispan.xsite.BackupReceiver} delegator. Mean to be overridden. For test purpose only!
 *
 * @author Pedro Ruivo
 * @since 7.0
 */
public abstract class BackupReceiverDelegator implements BackupReceiver {

   protected final BackupReceiver delegate;

   protected BackupReceiverDelegator(BackupReceiver delegate) {
      if (delegate == null) {
         throw new NullPointerException("Delegate cannot be null");
      }
      this.delegate = delegate;
   }

   @Override
   public Cache getCache() {
      return delegate.getCache();
   }

   @Override
   public CompletionStage<Void> handleRemoteCommand(VisitableCommand command, boolean preserveOrder) {
      return delegate.handleRemoteCommand(command, preserveOrder);
   }

   @Override
   public CompletionStage<Void> handleStartReceivingStateTransfer(XSiteStateTransferStartReceiveCommand command) {
      return delegate.handleStartReceivingStateTransfer(command);
   }

   @Override
   public CompletionStage<Void> handleEndReceivingStateTransfer(XSiteStateTransferFinishReceiveCommand command) {
      return delegate.handleEndReceivingStateTransfer(command);
   }

   @Override
   public CompletionStage<Void> handleStateTransferState(XSiteStatePushCommand cmd) {
      return delegate.handleStateTransferState(cmd);
   }
}
