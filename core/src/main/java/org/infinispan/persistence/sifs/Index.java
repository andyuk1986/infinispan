package org.infinispan.persistence.sifs;

import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.time.TimeService;
import org.infinispan.commons.util.IntSet;
import org.infinispan.util.concurrent.AggregateCompletionStage;
import org.infinispan.util.concurrent.CompletionStages;
import org.infinispan.util.concurrent.NonBlockingManager;
import org.infinispan.util.logging.LogFactory;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Keeps the entry positions persisted in a file. It consists of couple of segments, each for one modulo-range of key's
 * hashcodes (according to DataContainer's key equivalence configuration) - writes to each index segment are performed
 * by single thread, having multiple segments spreads the load between them.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
class Index {
   private static final Log log = LogFactory.getLog(Index.class, Log.class);
   // PRE ISPN 13 GRACEFULLY VALUE = 0x512ACEF0;
   private static final int GRACEFULLY = 0x512ACEF1;
   private static final int DIRTY = 0xD112770C;
   // 4 bytes for graceful shutdown
   // 4 bytes for segment max (this way the index can be regenerated if number of segments change
   // 8 bytes root offset
   // 2 bytes root occupied
   // 8 bytes free block offset
   // 8 bytes number of elements
   private static final int INDEX_FILE_HEADER_SIZE = 34;

   private final NonBlockingManager nonBlockingManager;
   private final FileProvider fileProvider;
   private final Path indexDir;
   private final Compactor compactor;
   private final int minNodeSize;
   private final int maxNodeSize;
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Segment[] segments;
   private final TimeService timeService;

   private final FlowableProcessor<IndexRequest>[] flowableProcessors;

   public Index(NonBlockingManager nonBlockingManager, FileProvider fileProvider, Path indexDir, int segments,
                int minNodeSize, int maxNodeSize, TemporaryTable temporaryTable, Compactor compactor,
                TimeService timeService) throws IOException {
      this.nonBlockingManager = nonBlockingManager;
      this.fileProvider = fileProvider;
      this.compactor = compactor;
      this.timeService = timeService;
      this.indexDir = indexDir;
      this.minNodeSize = minNodeSize;
      this.maxNodeSize = maxNodeSize;
      indexDir.toFile().mkdirs();

      // If there are more index files than our segments, that means we can't load them
      boolean hasExtraIndexFile = fileExists(segments);

      this.segments = new Segment[segments];
      this.flowableProcessors = new FlowableProcessor[segments];
      for (int i = 0; i < segments; ++i) {
         UnicastProcessor<IndexRequest> flowableProcessor = UnicastProcessor.create();
         Segment segment = new Segment(i, temporaryTable, !hasExtraIndexFile);

         this.segments[i] = segment;
         // It is possible to write from multiple threads
         this.flowableProcessors[i] = flowableProcessor.toSerialized();
      }
   }

   boolean fileExists(int id) {
      File indexFileFile = new File(indexDir.toFile(), "index." + id);
      return indexFileFile.exists();
   }

   public static byte[] toIndexKey(int cacheSegment, org.infinispan.commons.io.ByteBuffer buffer) {
      int segmentBytes = UnsignedNumeric.sizeUnsignedInt(cacheSegment);
      byte[] indexKey = new byte[buffer.getLength() + segmentBytes];
      UnsignedNumeric.writeUnsignedInt(indexKey, 0, cacheSegment);
      System.arraycopy(buffer.getBuf(), buffer.getOffset(), indexKey, segmentBytes, buffer.getLength());

      return indexKey;
   }

   static byte[] toIndexKey(int cacheSegment, byte[] bytes) {
      int segmentBytes = UnsignedNumeric.sizeUnsignedInt(cacheSegment);
      byte[] indexKey = new byte[bytes.length + segmentBytes];
      UnsignedNumeric.writeUnsignedInt(indexKey, 0, cacheSegment);
      System.arraycopy(bytes, 0, indexKey, segmentBytes, bytes.length);

      return indexKey;
   }

   /**
    * @return True if the index was loaded from well persisted state
    */
   public boolean isLoaded() {
      for (Segment segment : segments) {
         if (!segment.loaded) return false;
      }
      return true;
   }

   /**
    * Get record or null if expired
    */
   public EntryRecord getRecord(Object key, int cacheSegment, org.infinispan.commons.io.ByteBuffer serializedKey) throws IOException {
      return getRecord(key, cacheSegment, toIndexKey(cacheSegment, serializedKey), IndexNode.ReadOperation.GET_RECORD);
   }

   /**
    * Get record (even if expired) or null if not present
    */
   public EntryRecord getRecordEvenIfExpired(Object key, int cacheSegment, byte[] serializedKey) throws IOException {
      return getRecord(key, cacheSegment, toIndexKey(cacheSegment, serializedKey), IndexNode.ReadOperation.GET_EXPIRED_RECORD);
   }

   private EntryRecord getRecord(Object key, int cacheSegment, byte[] indexKey, IndexNode.ReadOperation readOperation) throws IOException {
      int segment = (key.hashCode() & Integer.MAX_VALUE) % segments.length;
      lock.readLock().lock();
      try {
         return IndexNode.applyOnLeaf(segments[segment], cacheSegment, indexKey, segments[segment].rootReadLock(), readOperation);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get position or null if expired
    */
   public EntryPosition getPosition(Object key, int cacheSegment, org.infinispan.commons.io.ByteBuffer serializedKey) throws IOException {
      int segment = (key.hashCode() & Integer.MAX_VALUE) % segments.length;
      lock.readLock().lock();
      try {
         return IndexNode.applyOnLeaf(segments[segment], cacheSegment, toIndexKey(cacheSegment, serializedKey), segments[segment].rootReadLock(), IndexNode.ReadOperation.GET_POSITION);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get position + numRecords, without expiration
    */
   public EntryInfo getInfo(Object key, int cacheSegment, byte[] serializedKey) throws IOException {
      int segment = (key.hashCode() & Integer.MAX_VALUE) % segments.length;
      lock.readLock().lock();
      try {
         return IndexNode.applyOnLeaf(segments[segment], cacheSegment, toIndexKey(cacheSegment, serializedKey), segments[segment].rootReadLock(), IndexNode.ReadOperation.GET_INFO);
      } finally {
         lock.readLock().unlock();
      }
   }

   public CompletionStage<Void> clear() {
      lock.writeLock().lock();
      try {
         AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
         for (FlowableProcessor<IndexRequest> processor : flowableProcessors) {
            IndexRequest clearRequest = IndexRequest.clearRequest();
            processor.onNext(clearRequest);
            stage.dependsOn(clearRequest);
         }
         return stage.freeze();
      } finally {
         lock.writeLock().unlock();
      }
   }

   public CompletionStage<Object> handleRequest(IndexRequest indexRequest) {
      int processor = (indexRequest.getKey().hashCode() & Integer.MAX_VALUE) % segments.length;
      flowableProcessors[processor].onNext(indexRequest);
      return indexRequest;
   }

   public void deleteFileAsync(int fileId) {
      AtomicInteger count = new AtomicInteger(flowableProcessors.length);
      for (FlowableProcessor<IndexRequest> flowableProcessor : flowableProcessors) {
         IndexRequest deleteFile = IndexRequest.syncRequest(() -> {
            // After all indexes have ensured they have processed all requests - the last one will delete the file
            // This guarantees that the index can't see an outdated value
            if (count.decrementAndGet() == 0) {
               fileProvider.deleteFile(fileId);
               log.tracef("Deleted file %s", fileId);
               compactor.releaseStats(fileId);
            }
         });
         flowableProcessor.onNext(deleteFile);
      }
   }

   public CompletionStage<Void> stop() throws InterruptedException {
      for (FlowableProcessor<IndexRequest> flowableProcessor : flowableProcessors) {
         flowableProcessor.onComplete();
      }

      AggregateCompletionStage<Void> aggregateCompletionStage = CompletionStages.aggregateCompletionStage();
      for (Segment segment : segments) {
         aggregateCompletionStage.dependsOn(segment);
      }
      return aggregateCompletionStage.freeze();
   }

   public CompletionStage<Long> size() {
      AtomicLong size = new AtomicLong();
      AggregateCompletionStage<AtomicLong> aggregateCompletionStage = CompletionStages.aggregateCompletionStage(size);
      for (FlowableProcessor<IndexRequest> flowableProcessor : flowableProcessors) {
         IndexRequest request = IndexRequest.sizeRequest();
         flowableProcessor.onNext(request);
         aggregateCompletionStage.dependsOn(request.thenAccept(count -> size.addAndGet((long) count)));
      }
      return aggregateCompletionStage.freeze().thenApply(AtomicLong::get);
   }

   public long approximateSize(IntSet cacheSegments) {
      long size = 0;
      for (PrimitiveIterator.OfInt segIter = cacheSegments.iterator(); segIter.hasNext(); ) {
         int cacheSegment = segIter.nextInt();
         for (Segment seg : segments) {
            size += seg.sizePerSegment.get(cacheSegment);
            if (size < 0) {
               return Long.MAX_VALUE;
            }
         }
      }

      return size;
   }

   public long getMaxSeqId() throws IOException {
      long maxSeqId = 0;
      lock.readLock().lock();
      try {
         for (Segment seg : segments) {
            maxSeqId = Math.max(maxSeqId, IndexNode.calculateMaxSeqId(seg, seg.rootReadLock()));
         }
      } finally {
         lock.readLock().unlock();
      }
      return maxSeqId;
   }

   public void start(Executor executor) {

      // Now that all index segment files are created and marked as dirty we can delete the excess index file if present
      // If this file was present we assume all indexes are dirty since the number of indexes was reduced
      new File(indexDir.toFile(), "index." + segments.length).delete();

      for (int i = 0; i < segments.length; ++i) {
         Segment segment = segments[i];
         flowableProcessors[i]
               .observeOn(Schedulers.from(executor))
               .subscribe(segment, segment::completeExceptionally, segment);
      }
   }

   class Segment extends CompletableFuture<Void> implements Consumer<IndexRequest>, Action {
      private final TemporaryTable temporaryTable;
      private final TreeMap<Short, List<IndexSpace>> freeBlocks = new TreeMap<>();
      private final ReadWriteLock rootLock = new ReentrantReadWriteLock();
      private final boolean loaded;
      private final FileChannel indexFile;
      private final File indexCountFile;
      private long indexFileSize;
      final AtomicLongArray sizePerSegment;

      private volatile IndexNode root;


      private Segment(int id, TemporaryTable temporaryTable, boolean attemptLoad) throws IOException {
         this.temporaryTable = temporaryTable;
         this.sizePerSegment = new AtomicLongArray(temporaryTable.getSegmentMax());

         int segmentMax = temporaryTable.getSegmentMax();
         File indexFileFile = new File(indexDir.toFile(), "index." + id);
         this.indexFile = new RandomAccessFile(indexFileFile, "rw").getChannel();
         this.indexCountFile = new File(indexDir.toFile(), "index-count." + id);
         indexFile.position(0);
         ByteBuffer buffer = ByteBuffer.allocate(INDEX_FILE_HEADER_SIZE);
         int gracefulValue = -1, segmentValue = -1;
         boolean validIndex = false;
         if (attemptLoad && indexFile.size() >= INDEX_FILE_HEADER_SIZE && read(indexFile, buffer)
               && (gracefulValue = buffer.getInt(0)) == GRACEFULLY
               && (segmentValue = buffer.getInt(4)) == segmentMax) {
            try (RandomAccessFile indexCount = new RandomAccessFile(indexCountFile, "r")) {
               for (int i = 0; i < sizePerSegment.length(); ++i) {
                  long value = readUnsignedLong(indexCount);
                  sizePerSegment.set(i, value);
               }

               validIndex = true;
            } catch (IOException e) {
               log.tracef("Encountered IOException %s while reading index count file, assuming index dirty", e.getMessage());
            }
         } else {
            log.tracef("Index %d is not valid must rebuild, gracefulValue=%s segments=%d attemptLoad=%s", id, gracefulValue, segmentValue, attemptLoad);
         }
         if (validIndex) {
            long rootOffset = buffer.getLong(8);
            short rootOccupied = buffer.getShort(16);
            long freeBlocksOffset = buffer.getLong(18);
            root = new IndexNode(this, rootOffset, rootOccupied);
            loadFreeBlocks(freeBlocksOffset);
            indexFileSize = freeBlocksOffset;
            loaded = true;
         } else {
            this.indexFile.truncate(0);
            root = IndexNode.emptyWithLeaves(this);
            loaded = false;
            // reserve space for shutdown
            indexFileSize = INDEX_FILE_HEADER_SIZE;
         }
         this.indexCountFile.delete();
         buffer.putInt(0, DIRTY);
         buffer.position(0);
         buffer.limit(4);
         indexFile.position(0);

         write(indexFile, buffer);
      }

      // Here solely for BlockHound
      private long readUnsignedLong(DataInput input) throws IOException {
         return UnsignedNumeric.readUnsignedLong(input);
      }

      private void write(FileChannel indexFile, ByteBuffer buffer) throws IOException {
         do {
            int written = indexFile.write(buffer);
            if (written < 0) {
               throw new IllegalStateException("Cannot write to index file!");
            }
         } while (buffer.position() < buffer.limit());
      }

      private boolean read(FileChannel indexFile, ByteBuffer buffer) throws IOException {
         do {
            int read = indexFile.read(buffer);
            if (read < 0) {
               return false;
            }
         } while (buffer.position() < buffer.limit());
         return true;
      }

      @Override
      public void accept(IndexRequest request) throws Throwable {
         if (log.isTraceEnabled()) log.trace("Indexing " + request);
         IndexNode.OverwriteHook overwriteHook;
         IndexNode.RecordChange recordChange;
         switch (request.getType()) {
            case CLEAR:
               root = IndexNode.emptyWithLeaves(this);
               indexFile.truncate(0);
               indexFileSize = INDEX_FILE_HEADER_SIZE;
               freeBlocks.clear();
               for (int i = 0; i < sizePerSegment.length(); ++i) {
                  sizePerSegment.set(i, 0);
               }
               nonBlockingManager.complete(request, null);
               return;
            case SYNC_REQUEST:
               Runnable runnable = (Runnable) request.getKey();
               runnable.run();
               nonBlockingManager.complete(request, null);
               return;
            case MOVED:
               recordChange = IndexNode.RecordChange.MOVE;
               overwriteHook = new IndexNode.OverwriteHook() {
                  @Override
                  public boolean check(int oldFile, int oldOffset) {
                     return oldFile == request.getPrevFile() && oldOffset == request.getPrevOffset();
                  }

                  @Override
                  public void setOverwritten(int cacheSegment, boolean overwritten, int prevFile, int prevOffset) {
                     if (overwritten && request.getOffset() < 0 && request.getPrevOffset() >= 0) {
                        sizePerSegment.incrementAndGet(cacheSegment);
                     }
                  }
               };
               break;
            case UPDATE:
               recordChange = IndexNode.RecordChange.INCREASE;
               overwriteHook = (cacheSegment, overwritten, prevFile, prevOffset) -> {
                  nonBlockingManager.complete(request, overwritten);
                  if (request.getOffset() >= 0 && prevOffset < 0) {
                     sizePerSegment.incrementAndGet(cacheSegment);
                  } else if (request.getOffset() < 0 && prevOffset >= 0) {
                     sizePerSegment.decrementAndGet(cacheSegment);
                  }
               };
               break;
            case DROPPED:
               recordChange = IndexNode.RecordChange.DECREASE;
               overwriteHook = (cacheSegment, overwritten, prevFile, prevOffset) -> {
                  if (request.getPrevFile() == prevFile && request.getPrevOffset() == prevOffset) {
                     sizePerSegment.decrementAndGet(cacheSegment);
                  }
               };
               break;
            case FOUND_OLD:
               recordChange = IndexNode.RecordChange.INCREASE_FOR_OLD;
               overwriteHook = IndexNode.NOOP_HOOK;
               break;
            case SIZE:
               long size = 0;
               for (int i = 0; i < sizePerSegment.length(); ++i) {
                  size += sizePerSegment.get(i);
                  if (size < 0) {
                     size = Long.MAX_VALUE;
                     break;
                  }
               }
               nonBlockingManager.complete(request, size);
               return;
            default:
               throw new IllegalArgumentException(request.toString());
         }
         try {
            IndexNode.setPosition(root, request.getSegment(), request.getSerializedKey(), request.getFile(), request.getOffset(),
                  request.getSize(), overwriteHook, recordChange);
         } catch (IllegalStateException e) {
            request.completeExceptionally(e);
         }
         temporaryTable.removeConditionally(request.getSegment(), request.getKey(), request.getFile(), request.getOffset());
         if (request.getType() != IndexRequest.Type.UPDATE) {
            // The update type will complete it in the switch statement above
            nonBlockingManager.complete(request, null);
         }
      }

      // This is ran when the flowable ends either via normal termination or error
      @Override
      public void run() throws IOException {
         try {
            IndexSpace rootSpace = allocateIndexSpace(root.length());
            root.store(rootSpace);
            indexFile.position(indexFileSize);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(0, freeBlocks.size());
            write(indexFile, buffer);
            for (Map.Entry<Short, List<IndexSpace>> entry : freeBlocks.entrySet()) {
               List<IndexSpace> list = entry.getValue();
               int requiredSize = 8 + list.size() * 10;
               buffer = buffer.capacity() < requiredSize ? ByteBuffer.allocate(requiredSize) : buffer;
               buffer.position(0);
               buffer.limit(requiredSize);
               // TODO: change this to short
               buffer.putInt(entry.getKey());
               buffer.putInt(list.size());
               for (IndexSpace space : list) {
                  buffer.putLong(space.offset);
                  buffer.putShort(space.length);
               }
               buffer.flip();
               write(indexFile, buffer);
            }
            int headerWithoutMagic = INDEX_FILE_HEADER_SIZE - 8;
            buffer = buffer.capacity() < headerWithoutMagic ? ByteBuffer.allocate(headerWithoutMagic) : buffer;
            buffer.position(0);
            // we need to set limit ahead, otherwise the putLong could throw IndexOutOfBoundsException
            buffer.limit(headerWithoutMagic);
            buffer.putLong(0, rootSpace.offset);
            buffer.putShort(8, rootSpace.length);
            buffer.putLong(10, indexFileSize);
            indexFile.position(8);
            write(indexFile, buffer);
            buffer.position(0);
            buffer.limit(8);
            buffer.putInt(0, GRACEFULLY);
            buffer.putInt(4, temporaryTable.getSegmentMax());
            indexFile.position(0);
            write(indexFile, buffer);

            // Create the file first as it should not be present as we deleted during startup
            this.indexCountFile.createNewFile();
            try (FileOutputStream indexCountStream = new FileOutputStream(indexCountFile)) {
               for (int i = 0; i < sizePerSegment.length(); ++i) {
                  UnsignedNumeric.writeUnsignedLong(indexCountStream, sizePerSegment.get(i));
               }
            }

            complete(null);
         } catch (Throwable t) {
            completeExceptionally(t);
         }
      }

      private void loadFreeBlocks(long freeBlocksOffset) throws IOException {
         indexFile.position(freeBlocksOffset);
         ByteBuffer buffer = ByteBuffer.allocate(8);
         buffer.limit(4);
         if (!read(indexFile, buffer)) {
            throw new IOException("Cannot read free blocks lists!");
         }
         int numLists = buffer.getInt(0);
         for (int i = 0; i < numLists; ++i) {
            buffer.position(0);
            buffer.limit(8);
            if (!read(indexFile, buffer)) {
               throw new IOException("Cannot read free blocks lists!");
            }
            // TODO: change this to short
            int blockLength = buffer.getInt(0);
            assert blockLength <= Short.MAX_VALUE;
            int listSize = buffer.getInt(4);
            int requiredSize = 10 * listSize;
            buffer = buffer.capacity() < requiredSize ? ByteBuffer.allocate(requiredSize) : buffer;
            buffer.position(0);
            buffer.limit(requiredSize);
            if (!read(indexFile, buffer)) {
               throw new IOException("Cannot read free blocks lists!");
            }
            buffer.flip();
            ArrayList<IndexSpace> list = new ArrayList<>(listSize);
            for (int j = 0; j < listSize; ++j) {
               list.add(new IndexSpace(buffer.getLong(), buffer.getShort()));
            }
            freeBlocks.put((short) blockLength, list);
         }
      }

      public FileChannel getIndexFile() {
         return indexFile;
      }

      public FileProvider getFileProvider() {
         return fileProvider;
      }

      public Compactor getCompactor() {
         return compactor;
      }

      public IndexNode getRoot() {
         // this has to be called with rootLock locked!
         return root;
      }

      public void setRoot(IndexNode root) {
         rootLock.writeLock().lock();
         this.root = root;
         rootLock.writeLock().unlock();
      }

      public int getMaxNodeSize() {
         return maxNodeSize;
      }

      public int getMinNodeSize() {
         return minNodeSize;
      }

      // this should be accessed only from the updater thread
      IndexSpace allocateIndexSpace(short length) {
         Map.Entry<Short, List<IndexSpace>> entry = freeBlocks.ceilingEntry(length);
         if (entry == null || entry.getValue().isEmpty()) {
            long oldSize = indexFileSize;
            indexFileSize += length;
            return new IndexSpace(oldSize, length);
         } else {
            return entry.getValue().remove(entry.getValue().size() - 1);
         }
      }

      // this should be accessed only from the updater thread
      void freeIndexSpace(long offset, short length) {
         if (length <= 0) throw new IllegalArgumentException("Offset=" + offset + ", length=" + length);
         // TODO: fragmentation!
         // TODO: memory bounds!
         if (offset + length < indexFileSize) {
            freeBlocks.computeIfAbsent(length, k -> new ArrayList<>()).add(new IndexSpace(offset, length));
         } else {
            indexFileSize -= length;
            try {
               indexFile.truncate(indexFileSize);
            } catch (IOException e) {
               log.cannotTruncateIndex(e);
            }
         }
      }

      Lock rootReadLock() {
         return rootLock.readLock();
      }

      public TimeService getTimeService() {
         return timeService;
      }
   }

   /**
    * Offset-length pair
    */
   static class IndexSpace {
      protected long offset;
      protected short length;

      IndexSpace(long offset, short length) {
         this.offset = offset;
         this.length = length;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof IndexSpace)) return false;

         IndexSpace innerNode = (IndexSpace) o;

         return length == innerNode.length && offset == innerNode.offset;
      }

      @Override
      public int hashCode() {
         int result = (int) (offset ^ (offset >>> 32));
         result = 31 * result + length;
         return result;
      }

      @Override
      public String toString() {
         return String.format("[%d-%d(%d)]", offset, offset + length, length);
      }
   }

   <V> Flowable<EntryRecord> publish(IntSet cacheSegments, boolean loadValues) {
      return Flowable.fromArray(segments)
            .concatMap(segment -> segment.root.publish(cacheSegments, loadValues));
   }
}
