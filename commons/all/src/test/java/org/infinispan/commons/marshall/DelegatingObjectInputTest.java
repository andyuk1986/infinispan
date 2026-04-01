package org.infinispan.commons.marshall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Tests for {@link DelegatingObjectInput}.
 */
public class DelegatingObjectInputTest {

   private byte[] serialize(Runnable writer) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
         writer.run();
         // Use a helper that captures the oos
      }
      return baos.toByteArray();
   }

   private byte[] serializeValues() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeBoolean(true);
      oos.writeByte(42);
      oos.writeShort(1000);
      oos.writeChar('X');
      oos.writeInt(123456);
      oos.writeLong(9876543210L);
      oos.writeFloat(3.14f);
      oos.writeDouble(2.718);
      oos.writeUTF("hello");
      oos.writeObject("world");
      oos.flush();
      return baos.toByteArray();
   }

   @Test
   public void testReadAllTypes() throws Exception {
      byte[] data = serializeValues();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);

      assertEquals(true, doi.readBoolean());
      assertEquals(42, doi.readByte());
      assertEquals(1000, doi.readShort());
      assertEquals('X', doi.readChar());
      assertEquals(123456, doi.readInt());
      assertEquals(9876543210L, doi.readLong());
      assertEquals(3.14f, doi.readFloat(), 0.001f);
      assertEquals(2.718, doi.readDouble(), 0.001);
      assertEquals("hello", doi.readUTF());
      assertEquals("world", doi.readObject());

      doi.close();
   }

   @Test
   public void testReadSingleByte() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.write(65);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      int b = doi.read();
      assertEquals(65, b);
      doi.close();
   }

   @Test
   public void testReadByteArray() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.write(new byte[]{10, 20, 30});
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      byte[] buf = new byte[3];
      int read = doi.read(buf);
      assertTrue(read > 0);
      doi.close();
   }

   @Test
   public void testReadByteArrayWithOffset() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.write(new byte[]{1, 2, 3, 4, 5});
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      byte[] buf = new byte[10];
      int read = doi.read(buf, 2, 3);
      assertTrue(read > 0);
      doi.close();
   }

   @Test
   public void testReadFully() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.write(new byte[]{1, 2, 3, 4});
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      byte[] buf = new byte[4];
      doi.readFully(buf);
      assertEquals(1, buf[0]);
      assertEquals(2, buf[1]);
      assertEquals(3, buf[2]);
      assertEquals(4, buf[3]);
      doi.close();
   }

   @Test
   public void testReadFullyWithOffset() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.write(new byte[]{10, 20, 30});
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      byte[] buf = new byte[5];
      doi.readFully(buf, 1, 3);
      assertEquals(10, buf[1]);
      assertEquals(20, buf[2]);
      assertEquals(30, buf[3]);
      doi.close();
   }

   @Test
   public void testReadUnsignedByte() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeByte(200);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      int ub = doi.readUnsignedByte();
      assertEquals(200, ub);
      doi.close();
   }

   @Test
   public void testReadUnsignedShort() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeShort(50000);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      int us = doi.readUnsignedShort();
      assertEquals(50000, us);
      doi.close();
   }

   @Test
   public void testSkipBytes() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeInt(1);
      oos.writeInt(2);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      int skipped = doi.skipBytes(4);
      assertEquals(4, skipped);
      assertEquals(2, doi.readInt());
      doi.close();
   }

   @Test
   public void testSkip() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeInt(1);
      oos.writeInt(2);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      long skipped = doi.skip(4);
      assertEquals(4, skipped);
      doi.close();
   }

   @Test
   public void testAvailable() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeInt(42);
      oos.flush();

      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      DelegatingObjectInput doi = new DelegatingObjectInput(ois);
      int avail = doi.available();
      assertTrue(avail >= 0);
      doi.close();
   }
}
