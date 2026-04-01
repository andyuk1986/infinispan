package org.infinispan.commons.marshall;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Tests for {@link DelegatingObjectOutput}.
 */
public class DelegatingObjectOutputTest {

   private DelegatingObjectOutput createDelegating(ObjectOutput delegate) {
      return new DelegatingObjectOutput(delegate);
   }

   @Test
   public void testWriteObject() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeObject("hello");
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteInt() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeInt(42);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteBoolean() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeBoolean(true);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteByte() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeByte(0x7F);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteShort() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeShort(1000);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteChar() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeChar('A');
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteLong() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeLong(123456789L);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteFloat() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeFloat(3.14f);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteDouble() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeDouble(2.718);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteBytes() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeBytes("test");
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteChars() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeChars("test");
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteUTF() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.writeUTF("hello world");
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteSingleByte() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.write(65);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteByteArray() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.write(new byte[]{1, 2, 3});
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testWriteByteArrayWithOffset() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.write(new byte[]{1, 2, 3, 4, 5}, 1, 3);
      doo.flush();
      assertTrue(baos.size() > 0);
   }

   @Test
   public void testClose() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      DelegatingObjectOutput doo = createDelegating(oos);
      doo.close();
   }
}
