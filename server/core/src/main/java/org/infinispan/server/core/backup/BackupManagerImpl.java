package org.infinispan.server.core.backup;

import static org.infinispan.server.core.backup.Constants.WORKING_DIR;
import static org.infinispan.server.core.logging.Messages.MESSAGES;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.globalstate.impl.ConfigCacheLock;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.security.AuthorizationPermission;
import org.infinispan.security.Security;
import org.infinispan.security.actions.SecurityActions;
import org.infinispan.server.core.BackupManager;
import org.infinispan.server.core.logging.Log;
import org.infinispan.util.concurrent.BlockingManager;
import org.infinispan.util.logging.LogFactory;
import org.infinispan.util.logging.events.EventLogCategory;
import org.infinispan.util.logging.events.EventLogManager;
import org.infinispan.util.logging.events.EventLogger;

/**
 * @author Ryan Emerson
 * @since 12.0
 */
public class BackupManagerImpl implements BackupManager {

   private static final Log log = LogFactory.getLog(BackupManagerImpl.class, Log.class);

   final ParserRegistry parserRegistry;

   final BlockingManager blockingManager;
   final Path rootDir;
   final BackupReader reader;
   volatile EventLogger eventLogger;
   volatile ConfigCacheLock backupLock;
   volatile ConfigCacheLock restoreLock;
   private final EmbeddedCacheManager cacheManager;
   final Map<String, DefaultCacheManager> cacheManagers;
   final Map<String, BackupRequest> backupMap;
   final Map<String, CompletionStage<Void>> restoreMap;

   public BackupManagerImpl(BlockingManager blockingManager, DefaultCacheManager cm, Path dataRoot) {
      this.blockingManager = blockingManager;
      this.rootDir = dataRoot.resolve(WORKING_DIR);
      this.cacheManager = cm;
      this.cacheManagers = Collections.singletonMap(cm.getName(), cm);
      this.parserRegistry = new ParserRegistry();
      this.reader = new BackupReader(blockingManager, cacheManagers, parserRegistry);
      this.backupMap = new ConcurrentHashMap<>();
      this.restoreMap = new ConcurrentHashMap<>();
   }

   @Override
   public void init() throws IOException {
      Files.createDirectories(rootDir);
      this.backupLock = new ConfigCacheLock("backup", cacheManager);
      this.restoreLock = new ConfigCacheLock("restore", cacheManager);

      GlobalComponentRegistry gcr = SecurityActions.getGlobalComponentRegistry(cacheManager);
      this.eventLogger = Objects.requireNonNull(gcr.getComponent(EventLogManager.class).getEventLogger(), "Event logger not found");
   }

   @Override
   public Set<String> getBackupNames() {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      return new HashSet<>(backupMap.keySet());
   }

   @Override
   public Status getBackupStatus(String name) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);

      Status status = getBackupStatus(backupMap.get(name));
      log.tracef("Backup status %s = %s", name, status);
      return status;
   }

   @Override
   public Path getBackupLocation(String name) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      BackupRequest request = backupMap.get(name);
      Status status = getBackupStatus(request);
      if (status != Status.COMPLETE)
         return null;
      return request.future.join();
   }

   private Status getBackupStatus(BackupRequest request) {
      CompletableFuture<Path> future = request == null ? null : request.future;
      return getFutureStatus(future);
   }

   private Status getFutureStatus(CompletionStage<?> stage) {
      if (stage == null)
         return Status.NOT_FOUND;

      CompletableFuture<?> future = stage.toCompletableFuture();
      if (future.isCompletedExceptionally()) {
         showCFException(future);
         return Status.FAILED;
      }

      return future.isDone() ? Status.COMPLETE : Status.IN_PROGRESS;
   }

   private void showCFException(CompletableFuture<?> cf) {
      try {
         cf.get();
      } catch (Throwable e) {
         log.errorf(CompletableFutures.extractException(e), "Backup request failed");
      }
   }

   @Override
   public CompletionStage<Status> removeBackup(String name) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      BackupRequest request = backupMap.get(name);
      Status status = getBackupStatus(request);
      switch (status) {
         case NOT_FOUND:
            backupMap.remove(name);
            return CompletableFuture.completedFuture(status);
         case COMPLETE:
         case FAILED:
            backupMap.remove(name);
            return blockingManager.supplyBlocking(() -> {
               request.writer.cleanup();
               return Status.COMPLETE;
            }, "remove-completed-backup");
         case IN_PROGRESS:
            // The backup files are removed on exceptional or successful completion.
            blockingManager.handleBlocking(request.future, (path, t) -> {
               // Regardless of whether the backup completes exceptionally or successfully, we remove the files
               request.writer.cleanup();
               return null;
            }, "remove-inprogress-backup");
            return CompletableFuture.completedFuture(Status.IN_PROGRESS);
      }
      throw new IllegalStateException();
   }

   @Override
   public CompletionStage<Path> create(String name, Path workingDir) {
      return create(
            name,
            workingDir,
            cacheManagers.entrySet().stream()
                  .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        p -> new BackupManagerResources.Builder().includeAll().build()))
      );
   }

   @Override
   public CompletionStage<Path> create(String name, Path workingDir, Map<String, Resources> params) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      if (getBackupStatus(name) != Status.NOT_FOUND)
         return CompletableFuture.failedFuture(log.backupAlreadyExists(name));

      BackupWriter writer = new BackupWriter(name, eventLogger, blockingManager, cacheManagers, parserRegistry, workingDir == null ? rootDir : workingDir);
      CompletionStage<Path> backupStage = backupLock.tryLock()
            .thenCompose(lockAcquired -> {
               log.tracef("Backup %s locked = %s", backupLock, lockAcquired);
               if (!lockAcquired)
                  return CompletableFuture.failedFuture(log.backupInProgress());

               log.initiatingBackup(name);
               return writer.create(params);
            });

      backupStage = CompletionStages.handleAndCompose(backupStage,
            (path, t) -> {
               CompletionStage<Void> unlock = backupLock.unlock().thenAccept(v -> log.tracef("Backup %s unlocked", backupLock));
               if (t != null) {
                  Throwable backupErr = log.errorCreatingBackup(t);
                  log.errorf(backupErr.getCause(), "%s:", backupErr.getMessage());
                  return unlock.thenCompose(ignore ->
                        CompletableFuture.failedFuture(backupErr)
                  );
               }
               log.backupComplete(path.getFileName().toString());
               eventLogger.info(EventLogCategory.LIFECYCLE, MESSAGES.backupCreated(name));
               return unlock.thenCompose(ignore -> CompletableFuture.completedFuture(path));
            });

      backupMap.put(name, new BackupRequest(writer, backupStage));
      return backupStage;
   }

   @Override
   public CompletionStage<Status> removeRestore(String name) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      CompletionStage<Void> stage = restoreMap.remove(name);
      Status status = getFutureStatus(stage);
      return CompletableFuture.completedFuture(status);
   }

   @Override
   public Status getRestoreStatus(String name) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      return getFutureStatus(restoreMap.get(name));
   }

   @Override
   public Set<String> getRestoreNames() {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      return new HashSet<>(restoreMap.keySet());
   }

   @Override
   public CompletionStage<Void> restore(String name, Path backup) {
      return restore(
            name,
            backup,
            cacheManagers.entrySet().stream()
                  .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        p -> new BackupManagerResources.Builder().includeAll().build()))
      );
   }

   @Override
   public CompletionStage<Void> restore(String name, Path backup, Map<String, Resources> params) {
      SecurityActions.checkPermission(cacheManager.withSubject(Security.getSubject()), AuthorizationPermission.ADMIN);
      if (getRestoreStatus(name) != Status.NOT_FOUND)
         return CompletableFuture.failedFuture(log.restoreAlreadyExists(name));

      if (!Files.exists(backup)) {
         CacheException e = log.errorRestoringBackup(backup, new FileNotFoundException(backup.toString()));
         log.errorf(e.getCause(), "%s:", e.getMessage());
         return CompletableFuture.failedFuture(e);
      }

      CompletionStage<Void> restoreStage = restoreLock.tryLock()
            .thenCompose(lockAcquired -> {
               if (!lockAcquired)
                  return CompletableFuture.failedFuture(log.restoreInProgress());

               log.initiatingRestore(name, backup);
               return reader.restore(backup, params);
            });

      restoreStage = CompletionStages.handleAndCompose(restoreStage,
            (path, t) -> {
               CompletionStage<Void> unlock = restoreLock.unlock();
               if (t != null) {
                  Throwable restoreErr = log.errorRestoringBackup(backup, t);
                  log.errorf(restoreErr.getCause(), "%s:", restoreErr.getMessage());
                  return unlock.thenCompose(ignore ->
                        CompletableFuture.failedFuture(restoreErr)
                  );
               }
               log.restoreComplete(name);
               eventLogger.info(EventLogCategory.LIFECYCLE, MESSAGES.backupRestored(name));
               return unlock.thenCompose(ignore -> CompletableFuture.completedFuture(path));
            });
      restoreMap.put(name, restoreStage);
      return restoreStage;
   }

   static class BackupRequest {
      final BackupWriter writer;
      final CompletableFuture<Path> future;

      BackupRequest(BackupWriter writer, CompletionStage<Path> stage) {
         this.writer = writer;
         this.future = stage.toCompletableFuture();
      }
   }
}
