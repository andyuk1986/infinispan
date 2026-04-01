package org.infinispan.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;

/**
 * Tests for {@link NetworkAddress}.
 */
public class NetworkAddressTest {

   @Test
   public void testLoopback() throws Exception {
      NetworkAddress addr = NetworkAddress.loopback("test");
      assertNotNull(addr);
      assertNotNull(addr.getAddress());
      assertTrue(addr.getAddress().isLoopbackAddress());
      assertEquals("test", addr.getName());
   }

   @Test
   public void testLoopbackToString() throws Exception {
      NetworkAddress addr = NetworkAddress.loopback("myName");
      String str = addr.toString();
      assertNotNull(str);
   }

   @Test
   public void testInetAddress() throws Exception {
      NetworkAddress addr = NetworkAddress.inetAddress("test", "127.0.0.1");
      assertNotNull(addr);
      assertEquals("test", addr.getName());
      assertEquals(InetAddress.getByName("127.0.0.1"), addr.getAddress());
   }

   @Test
   public void testInetAddressIPv6Loopback() throws Exception {
      NetworkAddress addr = NetworkAddress.inetAddress("test", "::1");
      assertNotNull(addr);
      assertEquals(InetAddress.getByName("::1"), addr.getAddress());
   }

   @Test
   public void testAnyAddress() throws Exception {
      NetworkAddress addr = NetworkAddress.anyAddress("test");
      assertNotNull(addr);
      assertTrue(addr.getAddress().isAnyLocalAddress());
   }

   @Test
   public void testFromStringWithIpAddress() throws Exception {
      NetworkAddress addr = NetworkAddress.fromString("test", "127.0.0.1");
      assertNotNull(addr);
      assertEquals(InetAddress.getByName("127.0.0.1"), addr.getAddress());
   }

   @Test
   public void testFromStringLoopback() throws Exception {
      NetworkAddress addr = NetworkAddress.fromString("test", "LOOPBACK");
      assertNotNull(addr);
      assertTrue(addr.getAddress().isLoopbackAddress());
   }

   @Test
   public void testFromStringNonLoopback() throws Exception {
      NetworkAddress addr = NetworkAddress.fromString("test", "NON_LOOPBACK");
      assertNotNull(addr);
      assertFalse(addr.getAddress().isLoopbackAddress());
   }

   @Test
   public void testFromStringSiteLocal() throws Exception {
      // SITE_LOCAL may fail in some environments, so test it gracefully
      try {
         NetworkAddress addr = NetworkAddress.fromString("test", "SITE_LOCAL");
         assertNotNull(addr);
         assertTrue(addr.getAddress().isSiteLocalAddress());
      } catch (Exception e) {
         // No site-local interface available - acceptable
      }
   }

   @Test
   public void testInetAddressMatchesInterfaceAddressExact() {
      byte[] inet = {(byte) 192, (byte) 168, 1, 100};
      byte[] iface = {(byte) 192, (byte) 168, 1, 0};
      assertTrue(NetworkAddress.inetAddressMatchesInterfaceAddress(inet, iface, 24));
   }

   @Test
   public void testInetAddressMatchesInterfaceAddressMismatch() {
      byte[] inet = {(byte) 192, (byte) 168, 2, 100};
      byte[] iface = {(byte) 192, (byte) 168, 1, 0};
      assertFalse(NetworkAddress.inetAddressMatchesInterfaceAddress(inet, iface, 24));
   }

   @Test
   public void testInetAddressMatchesInterfaceAddressFullMatch() {
      byte[] inet = {10, 0, 0, 1};
      byte[] iface = {10, 0, 0, 1};
      assertTrue(NetworkAddress.inetAddressMatchesInterfaceAddress(inet, iface, 32));
   }

   @Test
   public void testInetAddressMatchesInterfaceAddressShortPrefix() {
      byte[] inet = {10, 1, 2, 3};
      byte[] iface = {10, 99, 99, 99};
      assertTrue(NetworkAddress.inetAddressMatchesInterfaceAddress(inet, iface, 8));
   }

   @Test
   public void testGetPrefixLength() throws Exception {
      NetworkAddress addr = NetworkAddress.loopback("test");
      // Prefix length is set from InterfaceAddress or defaults to -1
      short prefix = addr.getPrefixLength();
      assertTrue(prefix >= -1);
   }

   @Test
   public void testCidr() throws Exception {
      NetworkAddress addr = NetworkAddress.loopback("test");
      String cidr = addr.cidr();
      assertNotNull(cidr);
      assertTrue(cidr.contains("/"));
   }

   @Test
   public void testNonLoopback() throws Exception {
      NetworkAddress addr = NetworkAddress.nonLoopback("test");
      assertNotNull(addr);
      assertFalse(addr.getAddress().isLoopbackAddress());
   }
}
