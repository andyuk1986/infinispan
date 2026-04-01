package org.infinispan.commons.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Tests for {@link UnsignedNumeric}.
 */
public class UnsignedNumericTest {

   private byte[] writeIntViaDataOutput(int value) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutput out = new DataOutputStream(baos);
      UnsignedNumeric.writeUnsignedInt(out, value);
      return baos.toByteArray();
   }

   private int readIntViaDataInput(byte[] bytes) throws IOException {
      DataInput in = new DataInputStream(new ByteArrayInputStream(bytes));
      return UnsignedNumeric.readUnsignedInt(in);
   }

   private byte[] writeLongViaDataOutput(long value) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutput out = new DataOutputStream(baos);
      UnsignedNumeric.writeUnsignedLong(out, value);
      return baos.toByteArray();
   }

   private long readLongViaDataInput(byte[] bytes) throws IOException {
      DataInput in = new DataInputStream(new ByteArrayInputStream(bytes));
      return UnsignedNumeric.readUnsignedLong(in);
   }

   // --- DataInput/DataOutput int tests ---

   @Test
   public void testIntZeroDataIO() throws IOException {
      byte[] bytes = writeIntViaDataOutput(0);
      assertEquals(1, bytes.length);
      assertEquals(0, readIntViaDataInput(bytes));
   }

   @Test
   public void testIntSmallDataIO() throws IOException {
      byte[] bytes = writeIntViaDataOutput(42);
      assertEquals(1, bytes.length);
      assertEquals(42, readIntViaDataInput(bytes));
   }

   @Test
   public void testIntOneByte() throws IOException {
      byte[] bytes = writeIntViaDataOutput(0x7F);
      assertEquals(1, bytes.length);
      assertEquals(0x7F, readIntViaDataInput(bytes));
   }

   @Test
   public void testIntTwoBytes() throws IOException {
      byte[] bytes = writeIntViaDataOutput(128);
      assertEquals(2, bytes.length);
      assertEquals(128, readIntViaDataInput(bytes));
   }

   @Test
   public void testIntLargeValue() throws IOException {
      int value = Integer.MAX_VALUE;
      byte[] bytes = writeIntViaDataOutput(value);
      assertEquals(5, bytes.length);
      assertEquals(value, readIntViaDataInput(bytes));
   }

   @Test
   public void testIntMediumValue() throws IOException {
      int value = 100000;
      byte[] bytes = writeIntViaDataOutput(value);
      assertEquals(value, readIntViaDataInput(bytes));
   }

   // --- InputStream/OutputStream int tests ---

   @Test
   public void testIntStreamIO() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      UnsignedNumeric.writeUnsignedInt(baos, 300);
      byte[] bytes = baos.toByteArray();
      int result = UnsignedNumeric.readUnsignedInt(new ByteArrayInputStream(bytes));
      assertEquals(300, result);
   }

   @Test
   public void testIntStreamZero() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      UnsignedNumeric.writeUnsignedInt(baos, 0);
      assertEquals(0, UnsignedNumeric.readUnsignedInt(new ByteArrayInputStream(baos.toByteArray())));
   }

   @Test
   public void testIntStreamLargeValue() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      UnsignedNumeric.writeUnsignedInt(baos, Integer.MAX_VALUE);
      assertEquals(Integer.MAX_VALUE, UnsignedNumeric.readUnsignedInt(new ByteArrayInputStream(baos.toByteArray())));
   }

   // --- ByteBuffer int tests ---

   @Test
   public void testIntByteBuffer() {
      ByteBuffer buf = ByteBuffer.allocate(10);
      UnsignedNumeric.writeUnsignedInt(buf, 500);
      buf.flip();
      assertEquals(500, UnsignedNumeric.readUnsignedInt(buf));
   }

   @Test
   public void testIntByteBufferZero() {
      ByteBuffer buf = ByteBuffer.allocate(10);
      UnsignedNumeric.writeUnsignedInt(buf, 0);
      buf.flip();
      assertEquals(0, UnsignedNumeric.readUnsignedInt(buf));
   }

   @Test
   public void testIntByteBufferMaxValue() {
      ByteBuffer buf = ByteBuffer.allocate(10);
      UnsignedNumeric.writeUnsignedInt(buf, Integer.MAX_VALUE);
      buf.flip();
      assertEquals(Integer.MAX_VALUE, UnsignedNumeric.readUnsignedInt(buf));
   }

   // --- byte[] int tests ---

   @Test
   public void testIntByteArray() {
      byte[] bytes = new byte[10];
      int written = UnsignedNumeric.writeUnsignedInt(bytes, 0, 12345);
      assertEquals(12345, UnsignedNumeric.readUnsignedInt(bytes, 0));
      assertEquals(written, UnsignedNumeric.sizeUnsignedInt(12345));
   }

   @Test
   public void testIntByteArrayZero() {
      byte[] bytes = new byte[10];
      int written = UnsignedNumeric.writeUnsignedInt(bytes, 0, 0);
      assertEquals(1, written);
      assertEquals(0, UnsignedNumeric.readUnsignedInt(bytes, 0));
   }

   @Test
   public void testIntByteArrayMaxValue() {
      byte[] bytes = new byte[10];
      UnsignedNumeric.writeUnsignedInt(bytes, 0, Integer.MAX_VALUE);
      assertEquals(Integer.MAX_VALUE, UnsignedNumeric.readUnsignedInt(bytes, 0));
   }

   @Test
   public void testIntByteArrayWithOffset() {
      byte[] bytes = new byte[20];
      int offset = 5;
      UnsignedNumeric.writeUnsignedInt(bytes, offset, 999);
      assertEquals(999, UnsignedNumeric.readUnsignedInt(bytes, offset));
   }

   // --- sizeUnsignedInt ---

   @Test
   public void testSizeUnsignedInt() {
      assertEquals(1, UnsignedNumeric.sizeUnsignedInt(0));
      assertEquals(1, UnsignedNumeric.sizeUnsignedInt(0x7F));
      assertEquals(2, UnsignedNumeric.sizeUnsignedInt(0x80));
      assertEquals(2, UnsignedNumeric.sizeUnsignedInt(0x3FFF));
      assertEquals(3, UnsignedNumeric.sizeUnsignedInt(0x4000));
      assertEquals(5, UnsignedNumeric.sizeUnsignedInt(Integer.MAX_VALUE));
   }

   // --- DataInput/DataOutput long tests ---

   @Test
   public void testLongZeroDataIO() throws IOException {
      byte[] bytes = writeLongViaDataOutput(0L);
      assertEquals(1, bytes.length);
      assertEquals(0L, readLongViaDataInput(bytes));
   }

   @Test
   public void testLongSmallDataIO() throws IOException {
      byte[] bytes = writeLongViaDataOutput(42L);
      assertEquals(42L, readLongViaDataInput(bytes));
   }

   @Test
   public void testLongLargeDataIO() throws IOException {
      long value = Long.MAX_VALUE;
      byte[] bytes = writeLongViaDataOutput(value);
      assertEquals(value, readLongViaDataInput(bytes));
   }

   @Test
   public void testLongMediumDataIO() throws IOException {
      long value = 1_000_000_000L;
      byte[] bytes = writeLongViaDataOutput(value);
      assertEquals(value, readLongViaDataInput(bytes));
   }

   // --- InputStream/OutputStream long tests ---

   @Test
   public void testLongStreamIO() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      UnsignedNumeric.writeUnsignedLong(baos, 100000L);
      assertEquals(100000L, UnsignedNumeric.readUnsignedLong(new ByteArrayInputStream(baos.toByteArray())));
   }

   @Test
   public void testLongStreamZero() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      UnsignedNumeric.writeUnsignedLong(baos, 0L);
      assertEquals(0L, UnsignedNumeric.readUnsignedLong(new ByteArrayInputStream(baos.toByteArray())));
   }

   // --- ByteBuffer long tests ---

   @Test
   public void testLongByteBuffer() {
      ByteBuffer buf = ByteBuffer.allocate(20);
      UnsignedNumeric.writeUnsignedLong(buf, 999999L);
      buf.flip();
      assertEquals(999999L, UnsignedNumeric.readUnsignedLong(buf));
   }

   @Test
   public void testLongByteBufferMaxValue() {
      ByteBuffer buf = ByteBuffer.allocate(20);
      UnsignedNumeric.writeUnsignedLong(buf, Long.MAX_VALUE);
      buf.flip();
      assertEquals(Long.MAX_VALUE, UnsignedNumeric.readUnsignedLong(buf));
   }

   // --- byte[] long tests ---

   @Test
   public void testLongByteArray() {
      byte[] bytes = new byte[20];
      UnsignedNumeric.writeUnsignedLong(bytes, 0, 54321L);
      assertEquals(54321L, UnsignedNumeric.readUnsignedLong(bytes, 0));
   }

   @Test
   public void testLongByteArrayZero() {
      byte[] bytes = new byte[20];
      UnsignedNumeric.writeUnsignedLong(bytes, 0, 0L);
      assertEquals(0L, UnsignedNumeric.readUnsignedLong(bytes, 0));
   }

   @Test
   public void testLongByteArrayMaxValue() {
      byte[] bytes = new byte[20];
      UnsignedNumeric.writeUnsignedLong(bytes, 0, Long.MAX_VALUE);
      assertEquals(Long.MAX_VALUE, UnsignedNumeric.readUnsignedLong(bytes, 0));
   }

   @Test
   public void testLongByteArrayWithOffset() {
      byte[] bytes = new byte[30];
      int offset = 7;
      UnsignedNumeric.writeUnsignedLong(bytes, offset, 123456789L);
      assertEquals(123456789L, UnsignedNumeric.readUnsignedLong(bytes, offset));
   }

   // --- Cross-format consistency ---

   @Test
   public void testIntConsistencyAcrossFormats() throws IOException {
      int value = 16384;
      // DataOutput
      byte[] dataBytes = writeIntViaDataOutput(value);
      // byte[]
      byte[] arrayBytes = new byte[10];
      UnsignedNumeric.writeUnsignedInt(arrayBytes, 0, value);
      // ByteBuffer
      ByteBuffer buf = ByteBuffer.allocate(10);
      UnsignedNumeric.writeUnsignedInt(buf, value);
      buf.flip();

      assertEquals(value, readIntViaDataInput(dataBytes));
      assertEquals(value, UnsignedNumeric.readUnsignedInt(arrayBytes, 0));
      assertEquals(value, UnsignedNumeric.readUnsignedInt(buf));
   }
}
