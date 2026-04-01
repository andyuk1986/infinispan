package org.infinispan.cli.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @since 16.2
 */
public class UtilsTest {

   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   @Test
   public void testSha256() throws IOException {
      Path file = tempFolder.newFile("test.txt").toPath();
      Files.writeString(file, "hello world");
      String hash = Utils.sha256(file);
      assertNotNull(hash);
      // SHA-256 of "hello world" is well-known
      assertEquals("B94D27B9934D3E08A52E52D7DA7DABFAC484EFE37A5380EE9088F7ACE2EFCDE9", hash);
   }

   @Test
   public void testDigestNonExistentFile() {
      Path nonExistent = Path.of("/nonexistent/file/path.txt");
      String result = Utils.digest(nonExistent, "SHA-256");
      assertNull(result);
   }

   @Test
   public void testRandomStringAlnum() {
      String result = Utils.randomString("[:alnum:]", 20);
      assertNotNull(result);
      assertEquals(20, result.length());
      assertTrue(result.chars().allMatch(c -> Character.isLetterOrDigit(c)));
   }

   @Test
   public void testRandomStringDigit() {
      String result = Utils.randomString("[:digit:]", 10);
      assertNotNull(result);
      assertEquals(10, result.length());
      assertTrue(result.chars().allMatch(Character::isDigit));
   }

   @Test
   public void testRandomStringAlpha() {
      String result = Utils.randomString("[:alpha:]", 15);
      assertNotNull(result);
      assertEquals(15, result.length());
      assertTrue(result.chars().allMatch(Character::isLetter));
   }

   @Test
   public void testRandomStringUpper() {
      String result = Utils.randomString("[:upper:]", 10);
      assertNotNull(result);
      assertEquals(10, result.length());
      assertTrue(result.chars().allMatch(Character::isUpperCase));
   }

   @Test
   public void testRandomStringLower() {
      String result = Utils.randomString("[:lower:]", 10);
      assertNotNull(result);
      assertEquals(10, result.length());
      assertTrue(result.chars().allMatch(Character::isLowerCase));
   }

   @Test
   public void testRandomStringXDigit() {
      String result = Utils.randomString("[:xdigit:]", 10);
      assertNotNull(result);
      assertEquals(10, result.length());
      assertTrue(result.chars().allMatch(c -> "0123456789abcdefABCDEF".indexOf(c) >= 0));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testRandomStringInvalidClass() {
      Utils.randomString("[:invalid:]", 10);
   }

   @Test
   public void testDigestWithAlgorithm() throws IOException {
      Path file = tempFolder.newFile("test2.txt").toPath();
      Files.writeString(file, "test content");
      String sha256 = Utils.digest(file, "SHA-256");
      assertNotNull(sha256);
      // Should be a hex string (64 chars for SHA-256)
      assertEquals(64, sha256.length());
   }
}
