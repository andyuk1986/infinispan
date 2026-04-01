package org.infinispan.remoting.transport;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

import org.testng.annotations.Test;

/**
 * Tests for {@link AggregateBackupResponse}.
 */
@Test(groups = "unit", testName = "remoting.transport.AggregateBackupResponseTest")
public class AggregateBackupResponseTest {

   private BackupResponse createMockResponse(Map<String, Throwable> failedBackups,
                                              Set<String> commErrors,
                                              long sendTime,
                                              boolean empty,
                                              boolean sync) {
      return new BackupResponse() {
         @Override
         public void waitForBackupToFinish() {
         }

         @Override
         public Map<String, Throwable> getFailedBackups() {
            return failedBackups;
         }

         @Override
         public Set<String> getCommunicationErrors() {
            return commErrors;
         }

         @Override
         public long getSendTimeMillis() {
            return sendTime;
         }

         @Override
         public boolean isEmpty() {
            return empty;
         }

         @Override
         public void notifyFinish(LongConsumer timeElapsedConsumer) {
            timeElapsedConsumer.accept(sendTime);
         }

         @Override
         public void notifyAsyncAck(XSiteAsyncAckListener listener) {
         }

         @Override
         public boolean isSync(String siteName) {
            return sync;
         }
      };
   }

   public void testWaitForBackupToFinish() throws Exception {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      aggregate.waitForBackupToFinish();
   }

   public void testGetFailedBackups() {
      Map<String, Throwable> failures1 = new HashMap<>();
      failures1.put("site1", new RuntimeException("fail1"));
      Map<String, Throwable> failures2 = new HashMap<>();
      failures2.put("site2", new RuntimeException("fail2"));

      BackupResponse r1 = createMockResponse(failures1, Collections.emptySet(), 100, false, false);
      BackupResponse r2 = createMockResponse(failures2, Collections.emptySet(), 200, false, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);

      Map<String, Throwable> allFailures = aggregate.getFailedBackups();
      assertEquals(2, allFailures.size());
      assertNotNull(allFailures.get("site1"));
      assertNotNull(allFailures.get("site2"));
   }

   public void testGetCommunicationErrors() {
      Set<String> errors1 = new HashSet<>(Collections.singleton("site1"));
      Set<String> errors2 = new HashSet<>(Collections.singleton("site2"));

      BackupResponse r1 = createMockResponse(Collections.emptyMap(), errors1, 100, false, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), errors2, 200, false, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);

      Set<String> allErrors = aggregate.getCommunicationErrors();
      assertEquals(2, allErrors.size());
      assertTrue(allErrors.contains("site1"));
      assertTrue(allErrors.contains("site2"));
   }

   public void testGetSendTimeMillisReturnsMin() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 50, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      assertEquals(50, aggregate.getSendTimeMillis());
   }

   public void testIsEmptyAllEmpty() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      assertTrue(aggregate.isEmpty());
   }

   public void testIsEmptyOneNonEmpty() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, false, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      assertFalse(aggregate.isEmpty());
   }

   public void testIsSyncTrue() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, true);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      assertTrue(aggregate.isSync("anySite"));
   }

   public void testIsSyncFalse() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);
      assertFalse(aggregate.isSync("anySite"));
   }

   public void testNotifyFinish() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      BackupResponse r2 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 200, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, r2);

      AtomicLong totalTime = new AtomicLong(0);
      aggregate.notifyFinish(totalTime::addAndGet);
      assertEquals(300, totalTime.get());
   }

   public void testConstructorWithNullResponses() {
      AggregateBackupResponse aggregate = new AggregateBackupResponse(null, null);
      assertTrue(aggregate.isEmpty());
   }

   public void testConstructorWithOneNullResponse() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, null);
      assertTrue(aggregate.isEmpty());
   }

   public void testToString() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      AggregateBackupResponse aggregate = new AggregateBackupResponse(r1, null);
      String str = aggregate.toString();
      assertNotNull(str);
      assertTrue(str.contains("AggregateBackupResponse"));
   }

   public void testEquals() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      AggregateBackupResponse a1 = new AggregateBackupResponse(r1, null);
      AggregateBackupResponse a2 = new AggregateBackupResponse(r1, null);
      assertTrue(a1.equals(a1));
      assertFalse(a1.equals(null));
      assertFalse(a1.equals("string"));
   }

   public void testHashCode() {
      BackupResponse r1 = createMockResponse(Collections.emptyMap(), Collections.emptySet(), 100, true, false);
      AggregateBackupResponse a1 = new AggregateBackupResponse(r1, null);
      // Should not throw
      a1.hashCode();
   }
}
