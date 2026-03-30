package org.infinispan.remoting.responses;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.infinispan.container.entries.ImmortalCacheValue;
import org.infinispan.container.entries.MortalCacheValue;
import org.infinispan.container.entries.TransientCacheValue;
import org.infinispan.container.entries.TransientMortalCacheValue;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "remoting.responses.ResponseTest")
public class ResponseTest extends AbstractInfinispanTest {

   // ---- CacheNotFoundResponse ----

   public void testCacheNotFoundResponse() {
      CacheNotFoundResponse response = CacheNotFoundResponse.INSTANCE;
      assertNotNull(response);
      assertFalse(response.isValid());
      assertFalse(response.isSuccessful());
   }

   public void testCacheNotFoundResponseSingleton() {
      assertTrue(CacheNotFoundResponse.INSTANCE == CacheNotFoundResponse.getInstance());
   }

   // ---- ExceptionResponse ----

   public void testExceptionResponse() {
      Exception ex = new RuntimeException("test error");
      ExceptionResponse response = new ExceptionResponse(ex);
      assertNotNull(response);
      assertFalse(response.isValid());
      assertFalse(response.isSuccessful());
      assertNotNull(response.getException());
      assertEquals("test error", response.getException().getMessage());
   }

   public void testExceptionResponseSetException() {
      ExceptionResponse response = new ExceptionResponse(new RuntimeException("old"));
      Exception newEx = new RuntimeException("new");
      response.setException(newEx);
      assertEquals("new", response.getException().getMessage());
   }

   public void testExceptionResponseToString() {
      ExceptionResponse response = new ExceptionResponse(new RuntimeException("test"));
      assertNotNull(response.toString());
   }

   // ---- UnsureResponse ----

   public void testUnsureResponse() {
      UnsureResponse response = UnsureResponse.INSTANCE;
      assertNotNull(response);
      assertTrue(response.isValid());
      assertFalse(response.isSuccessful());
   }

   public void testUnsureResponseGetValue() {
      try {
         UnsureResponse.INSTANCE.getResponseValue();
         assert false : "Expected UnsupportedOperationException";
      } catch (UnsupportedOperationException expected) {
      }
   }

   // ---- SuccessfulResponse.create factory ----

   public void testSuccessfulResponseCreateNull() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(null);
      assertNotNull(response);
      assertTrue(response.isValid());
      assertTrue(response.isSuccessful());
      assertNull(response.getResponseValue());
   }

   public void testSuccessfulResponseCreateLong() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(42L);
      assertNotNull(response);
      assertTrue(response instanceof SuccessfulLongResponse);
      assertEquals(42L, response.getResponseValue());
   }

   public void testSuccessfulResponseCreateBoolean() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(true);
      assertNotNull(response);
      assertTrue(response instanceof SuccessfulBooleanResponse);
      assertEquals(true, response.getResponseValue());
   }

   public void testSuccessfulResponseCreateBooleanFalse() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(false);
      assertNotNull(response);
      assertTrue(response instanceof SuccessfulBooleanResponse);
      assertEquals(false, response.getResponseValue());
   }

   public void testSuccessfulResponseCreateCollection() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(Arrays.asList("a", "b"));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateByteArray() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(new byte[]{1, 2, 3});
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateObjectArray() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(new Object[]{"a", "b"});
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateMap() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(Collections.singletonMap("k", "v"));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateString() {
      SuccessfulResponse<?> response = SuccessfulResponse.create("hello");
      assertNotNull(response);
      assertTrue(response.isSuccessful());
      assertEquals("hello", response.getResponseValue());
   }

   public void testSuccessfulResponseCreateImmortalCacheValue() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(new ImmortalCacheValue("val"));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateMortalCacheValue() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(new MortalCacheValue("val", 5000, 1000));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateTransientCacheValue() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(new TransientCacheValue("val", 3000, 1000));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   public void testSuccessfulResponseCreateTransientMortalCacheValue() {
      SuccessfulResponse<?> response = SuccessfulResponse.create(
            new TransientMortalCacheValue("val", 3000, 5000, 1000, 1000));
      assertNotNull(response);
      assertTrue(response.isSuccessful());
   }

   // ---- SuccessfulBooleanResponse ----

   public void testSuccessfulBooleanResponseEquals() {
      SuccessfulBooleanResponse r1 = new SuccessfulBooleanResponse(true);
      SuccessfulBooleanResponse r2 = new SuccessfulBooleanResponse(true);
      assertTrue(r1.equals(r2));
      assertEquals(r1.hashCode(), r2.hashCode());
   }

   public void testSuccessfulBooleanResponseNotEquals() {
      SuccessfulBooleanResponse r1 = new SuccessfulBooleanResponse(true);
      SuccessfulBooleanResponse r2 = new SuccessfulBooleanResponse(false);
      assertFalse(r1.equals(r2));
   }

   public void testSuccessfulBooleanResponseNotEqualsNull() {
      SuccessfulBooleanResponse r1 = new SuccessfulBooleanResponse(true);
      assertFalse(r1.equals(null));
   }

   public void testSuccessfulBooleanResponseNotEqualsDifferentClass() {
      SuccessfulBooleanResponse r1 = new SuccessfulBooleanResponse(true);
      assertFalse(r1.equals("notAResponse"));
   }

   public void testSuccessfulBooleanResponseToString() {
      SuccessfulBooleanResponse r = new SuccessfulBooleanResponse(true);
      assertNotNull(r.toString());
   }

   // ---- SuccessfulLongResponse ----

   public void testSuccessfulLongResponseEquals() {
      SuccessfulLongResponse r1 = new SuccessfulLongResponse(42L);
      SuccessfulLongResponse r2 = new SuccessfulLongResponse(42L);
      assertTrue(r1.equals(r2));
      assertEquals(r1.hashCode(), r2.hashCode());
   }

   public void testSuccessfulLongResponseNotEquals() {
      SuccessfulLongResponse r1 = new SuccessfulLongResponse(42L);
      SuccessfulLongResponse r2 = new SuccessfulLongResponse(99L);
      assertFalse(r1.equals(r2));
   }

   public void testSuccessfulLongResponseNotEqualsNull() {
      SuccessfulLongResponse r1 = new SuccessfulLongResponse(42L);
      assertFalse(r1.equals(null));
   }

   public void testSuccessfulLongResponseNotEqualsDifferentClass() {
      SuccessfulLongResponse r1 = new SuccessfulLongResponse(42L);
      assertFalse(r1.equals("notAResponse"));
   }

   public void testSuccessfulLongResponseToString() {
      SuccessfulLongResponse r = new SuccessfulLongResponse(42L);
      assertNotNull(r.toString());
   }

   // ---- UnsuccessfulResponse ----

   public void testUnsuccessfulResponse() {
      UnsuccessfulResponse<String> response = new UnsuccessfulResponse<>("val");
      assertTrue(response.isValid());
      assertFalse(response.isSuccessful());
      assertEquals("val", response.getResponseValue());
   }

   // ---- PrepareResponse ----

   public void testPrepareResponse() {
      PrepareResponse response = new PrepareResponse();
      assertTrue(response.isValid());
      assertTrue(response.isSuccessful());
   }

   public void testPrepareResponseGetValueUnsupported() {
      PrepareResponse response = new PrepareResponse();
      try {
         response.getResponseValue();
         assert false : "Expected UnsupportedOperationException";
      } catch (UnsupportedOperationException expected) {
      }
   }

   public void testPrepareResponseToString() {
      PrepareResponse response = new PrepareResponse();
      assertNotNull(response.toString());
   }
}
