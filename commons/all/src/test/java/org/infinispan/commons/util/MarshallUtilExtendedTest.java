package org.infinispan.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.infinispan.commons.marshall.MarshallUtil;
import org.junit.Test;

/**
 * Extended tests for {@link MarshallUtil} covering methods not tested by MarshallUtilTest.
 */
public class MarshallUtilExtendedTest {

   // --- Map marshalling ---

   @Test
   public void testMarshallMapDefaultSerialization() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      Map<String, String> map = new HashMap<>();
      map.put("key1", "val1");
      map.put("key2", "val2");
      MarshallUtil.marshallMap(map, io);

      Map<String, String> result = MarshallUtil.unmarshallMap(io, HashMap::new);
      assertEquals(2, result.size());
      assertEquals("val1", result.get("key1"));
      assertEquals("val2", result.get("key2"));
   }

   @Test
   public void testMarshallMapNull() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallMap(null, io);
      Map<String, String> result = MarshallUtil.unmarshallMap(io, HashMap::new);
      assertNull(result);
   }

   @Test
   public void testMarshallMapEmpty() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallMap(new HashMap<>(), io);
      Map<String, String> result = MarshallUtil.unmarshallMap(io, HashMap::new);
      assertNotNull(result);
      assertTrue(result.isEmpty());
   }

   @Test
   public void testMarshallMapWithCustomWriters() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      Map<String, Integer> map = new HashMap<>();
      map.put("a", 1);
      map.put("b", 2);

      MarshallUtil.marshallMap(map,
            (out, key) -> out.writeObject(key),
            (out, val) -> out.writeObject(val),
            io);

      Map<String, Integer> result = MarshallUtil.unmarshallMap(io,
            in -> (String) in.readObject(),
            in -> (Integer) in.readObject(),
            HashMap::new);
      assertEquals(2, result.size());
      assertEquals(Integer.valueOf(1), result.get("a"));
      assertEquals(Integer.valueOf(2), result.get("b"));
   }

   @Test
   public void testMarshallMapWithCustomWritersNull() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallMap(null,
            (out, key) -> out.writeObject(key),
            (out, val) -> out.writeObject(val),
            io);

      Map<String, Integer> result = MarshallUtil.unmarshallMap(io,
            in -> (String) in.readObject(),
            in -> (Integer) in.readObject(),
            HashMap::new);
      assertNull(result);
   }

   // --- Collection marshalling ---

   @Test
   public void testMarshallCollectionDefaultSerialization() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      List<String> list = new ArrayList<>(Arrays.asList("x", "y", "z"));
      MarshallUtil.marshallCollection(list, io);

      List<String> result = MarshallUtil.unmarshallCollection(io, ArrayList::new);
      assertEquals(3, result.size());
      assertEquals("x", result.get(0));
      assertEquals("y", result.get(1));
      assertEquals("z", result.get(2));
   }

   @Test
   public void testMarshallCollectionNull() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallCollection(null, io);
      List<String> result = MarshallUtil.unmarshallCollection(io, ArrayList::new);
      assertNull(result);
   }

   @Test
   public void testMarshallCollectionWithCustomWriter() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
      MarshallUtil.marshallCollection(set, io, (out, e) -> out.writeObject(e));

      Set<String> result = MarshallUtil.unmarshallCollection(io,
            HashSet::new,
            in -> (String) in.readObject());
      assertEquals(2, result.size());
      assertTrue(result.contains("a"));
      assertTrue(result.contains("b"));
   }

   @Test
   public void testUnmarshallCollectionUnbounded() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      List<String> list = new ArrayList<>(Arrays.asList("p", "q"));
      MarshallUtil.marshallCollection(list, io);

      List<String> result = MarshallUtil.unmarshallCollectionUnbounded(io, ArrayList::new);
      assertEquals(2, result.size());
      assertEquals("p", result.get(0));
      assertEquals("q", result.get(1));
   }

   // --- Int collection marshalling ---

   @Test
   public void testMarshallIntCollection() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      List<Integer> list = new ArrayList<>(Arrays.asList(10, 20, 30));
      MarshallUtil.marshallIntCollection(list, io);

      List<Integer> result = MarshallUtil.unmarshallIntCollection(io, ArrayList::new);
      assertEquals(3, result.size());
      assertEquals(Integer.valueOf(10), result.get(0));
      assertEquals(Integer.valueOf(20), result.get(1));
      assertEquals(Integer.valueOf(30), result.get(2));
   }

   @Test
   public void testMarshallIntCollectionNull() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallIntCollection(null, io);
      List<Integer> result = MarshallUtil.unmarshallIntCollection(io, ArrayList::new);
      assertNull(result);
   }

   // --- String marshalling ---

   @Test
   public void testMarshallString() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallString("hello", io);
      assertEquals("hello", MarshallUtil.unmarshallString(io));
   }

   @Test
   public void testMarshallStringNull() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallString(null, io);
      assertNull(MarshallUtil.unmarshallString(io));
   }

   @Test
   public void testMarshallStringEmpty() throws Exception {
      ObjectInputOutput io = new ObjectInputOutput();
      MarshallUtil.marshallString("", io);
      assertEquals("", MarshallUtil.unmarshallString(io));
   }

   // --- isSafeClass ---

   @Test
   public void testIsSafeClassExactMatch() {
      List<String> allowList = Arrays.asList("java.lang.String");
      assertTrue(MarshallUtil.isSafeClass("java.lang.String", allowList));
   }

   @Test
   public void testIsSafeClassNoMatch() {
      List<String> allowList = Arrays.asList("java.lang.String");
      assertFalse(MarshallUtil.isSafeClass("java.lang.Integer", allowList));
   }

   @Test
   public void testIsSafeClassWildcard() {
      List<String> allowList = Arrays.asList("java.lang.*");
      assertTrue(MarshallUtil.isSafeClass("java.lang.String", allowList));
      assertTrue(MarshallUtil.isSafeClass("java.lang.Integer", allowList));
   }

   @Test
   public void testIsSafeClassRegex() {
      List<String> allowList = Arrays.asList("java\\.util\\..*");
      assertTrue(MarshallUtil.isSafeClass("java.util.ArrayList", allowList));
      assertFalse(MarshallUtil.isSafeClass("java.lang.String", allowList));
   }

   @Test
   public void testIsSafeClassEmptyList() {
      assertFalse(MarshallUtil.isSafeClass("java.lang.String", new ArrayList<>()));
   }

   // Reuse the ObjectInputOutput from MarshallUtilTest
   private static class ObjectInputOutput implements ObjectOutput, ObjectInput {

      private final Queue<Object> buffer = new LinkedList<>();

      @Override
      public void writeByte(int v) {
         buffer.add((byte) v);
      }

      @Override
      public void writeShort(int v) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeChar(int v) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeInt(int v) {
         buffer.add(v);
      }

      @Override
      public void writeLong(long v) {
         buffer.add(v);
      }

      @Override
      public void writeFloat(float v) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeDouble(double v) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeBytes(String s) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeChars(String s) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeUTF(String s) {
         buffer.add(s);
      }

      @Override
      public Object readObject() {
         return buffer.poll();
      }

      @Override
      public int read() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int read(byte[] b) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int read(byte[] b, int off, int len) {
         throw new UnsupportedOperationException();
      }

      @Override
      public long skip(long n) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int available() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void readFully(byte[] b) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void readFully(byte[] b, int off, int len) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int skipBytes(int n) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean readBoolean() {
         return (boolean) buffer.poll();
      }

      @Override
      public byte readByte() {
         return (byte) buffer.poll();
      }

      @Override
      public int readUnsignedByte() {
         return Byte.toUnsignedInt((byte) buffer.poll());
      }

      @Override
      public short readShort() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int readUnsignedShort() {
         throw new UnsupportedOperationException();
      }

      @Override
      public char readChar() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int readInt() {
         return (int) buffer.poll();
      }

      @Override
      public long readLong() {
         return (long) buffer.poll();
      }

      @Override
      public float readFloat() {
         throw new UnsupportedOperationException();
      }

      @Override
      public double readDouble() {
         throw new UnsupportedOperationException();
      }

      @Override
      public String readLine() {
         throw new UnsupportedOperationException();
      }

      @Override
      public String readUTF() {
         return (String) buffer.poll();
      }

      @Override
      public void writeObject(Object obj) {
         buffer.add(obj);
      }

      @Override
      public void write(int b) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void write(byte[] b) {
         buffer.add(b);
      }

      @Override
      public void write(byte[] b, int off, int len) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeBoolean(boolean v) {
         buffer.add(v);
      }

      @Override
      public void flush() {
      }

      @Override
      public void close() {
      }
   }
}
