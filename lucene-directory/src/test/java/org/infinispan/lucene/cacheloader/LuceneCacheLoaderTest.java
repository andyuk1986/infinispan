/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.infinispan.lucene.cacheloader;

import org.infinispan.CacheException;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderManager;
import org.infinispan.lucene.ChunkCacheKey;
import org.infinispan.lucene.FileCacheKey;
import org.infinispan.lucene.FileListCacheKey;
import org.infinispan.lucene.FileReadLockKey;
import org.infinispan.lucene.InfinispanDirectory;
import org.infinispan.lucene.cachestore.LuceneCacheLoader;
import org.infinispan.lucene.cachestore.LuceneCacheLoaderConfig;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.TestingUtil;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Tests covering LuceneCacheLoader methods.
 *
 * @author Anna Manukyan
 */
@Test(groups = "functional", testName = "lucene.cacheloader.LuceneCacheLoaderTest")
public class LuceneCacheLoaderTest extends IndexCacheLoaderTest {

   private String indexName = "index-A";
   private int elementCount = 10;

   @Test(expectedExceptions = CacheException.class)
   public void testLuceneCacheLoaderWithWrongDir() throws IOException {
      File file = new File("test.txt");
      boolean created = file.createNewFile();
      file.deleteOnExit();

      assert created;

      EmbeddedCacheManager cacheManager = null;
      try {
         cacheManager = initializeInfinispan(file, indexName);
         InfinispanDirectory directory = new InfinispanDirectory(cacheManager.getCache(), indexName);
      } finally {
         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(expectedExceptions = CacheException.class)
   public void testLuceneCacheLoaderWithNonReadableDir() throws IOException {
      File rootDir = new File(new File(parentDir), rootDirectoryName);
      boolean directoriesCreated = rootDir.mkdirs();
      rootDir.setReadable(false);

      assert directoriesCreated;

      EmbeddedCacheManager cacheManager = null;
      try {
         cacheManager = initializeInfinispan(rootDir, indexName);
         InfinispanDirectory directory = new InfinispanDirectory(cacheManager.getCache(), indexName);
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);
         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(dataProvider = "rootDirProvider")
   public void testContainsKeyWithNoExistentRootDir(File rootDir) throws IOException, CacheLoaderException {
      EmbeddedCacheManager cacheManager = null;
      try {
         cacheManager = initializeInfinispan(rootDir, indexName);
         InfinispanDirectory directory = new InfinispanDirectory(cacheManager.getCache(), indexName);

         createIndex(rootDir, indexName, elementCount, true);
         verifyOnDirectory(directory, indexName, elementCount, true);

         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();
         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.containsKey(key);

            //Testing non-existent keys with non-acceptable type
            assert !cacheLoader.containsKey(fileName);
         }

      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testContainsKeyCacheKeyTypes() throws Exception {
      EmbeddedCacheManager cacheManager = null;
      File rootDir = createRootDir();
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         assert cacheLoader.containsKey(new FileListCacheKey(indexName));

         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);
         for(String fileName : fileNamesFromIndexDir) {
            assert !cacheLoader.containsKey(new FileReadLockKey(indexName, fileName)) : "Failed for " + fileName;
            assert cacheLoader.containsKey(new ChunkCacheKey(indexName, fileName, 0, 1024)) : "Failed for " + fileName;
         }

         assert !cacheLoader.containsKey(new ChunkCacheKey(indexName, "testFile.txt", 0, 1024));
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoadKey() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();
         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.load(key) != null;

            //Testing non-existent keys with non-acceptable type
            assert cacheLoader.load(fileName) == null;
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(expectedExceptions = CacheLoaderException.class)
   public void testLoadKeyWithNonExistentFile() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         cacheManager = initializeInfinispan(rootDir, indexName);
         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();
         FileCacheKey key = new FileCacheKey(indexName, "testKey");
         assert cacheLoader.load(key) == null;
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(expectedExceptions = CacheLoaderException.class)
   public void testLoadKeyWithInnerNonReadableDir() throws Exception {
      File rootDir = createRootDir();

      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         File innerDir = new File(rootDir, "index-B");
         boolean created = innerDir.mkdirs();
         assert created;

         innerDir.setReadable(false);
         innerDir.setWritable(false);

         cacheLoader.load(5);
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoad0Entries() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         Set<InternalCacheEntry> loadedEntrySet = cacheLoader.load(0);
         assert loadedEntrySet.isEmpty();
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(dataProvider = "passEntriesCount")
   public void testLoadEntries(Integer entriesNum) throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         Set<InternalCacheEntry> loadedEntrySet = null;
         if(entriesNum != null) {
            loadedEntrySet = cacheLoader.load(entriesNum.intValue());
         } else {
            loadedEntrySet = cacheLoader.loadAll();
         }

         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.load(key) != null;

            boolean found = false;
            for(InternalCacheEntry entry : loadedEntrySet) {
               FileCacheKey keyFromLoad = null;

               if(entry.getKey() instanceof FileCacheKey) {
                  keyFromLoad = (FileCacheKey) entry.getKey();

                  if (keyFromLoad != null && keyFromLoad.equals(key)) {
                     found = true;
                     break;
                  }
               }
            }

            assert found : "No corresponding entry found for " + key;
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoadAllKeys() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         Set keyList = cacheLoader.loadAllKeys(new HashSet());
         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.load(key) != null;

            boolean found = false;
            for(Object keyFromList : keyList) {
               if(keyFromList instanceof FileCacheKey && keyFromList.equals(key)) {
                  found = true;
                  break;
               }
            }

            assert found : "No corresponding key was found for " + key;
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoadAllKeysWithExclusion() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         HashSet exclusionSet = new HashSet();
         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.load(key) != null;

            exclusionSet.add(key);
         }

         Set keyList = cacheLoader.loadAllKeys(exclusionSet);

         AssertJUnit.assertEquals(1, keyList.size());

         Iterator it = keyList.iterator();
         if(it.hasNext()) {
            assert it.next() instanceof FileListCacheKey;
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoadAllKeysWithExclusionOfRootKey() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         HashSet exclusionSet = new HashSet();
         exclusionSet.add(new FileListCacheKey(indexName));

         Set keyList = cacheLoader.loadAllKeys(exclusionSet);

         AssertJUnit.assertEquals(10, keyList.size());

         Iterator it = keyList.iterator();
         while (it.hasNext()) {
            assert !(it.next() instanceof FileListCacheKey);
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testLoadAllKeysWithChunkExclusion() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);
         cacheManager = initializeInfinispan(rootDir, indexName);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         HashSet exclusionSet = new HashSet();
         String[] fileNames = getFileNamesFromDir(rootDir);
         for(String fileName : fileNames) {
            exclusionSet.add(new ChunkCacheKey(indexName, fileName, 0, 1024));
         }

         Set keyList = cacheLoader.loadAllKeys(exclusionSet);

         AssertJUnit.assertEquals(11, keyList.size());
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @Test(enabled = false)
   public void testLoadAllKeysWithNullExclusion() throws Exception {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         createIndex(rootDir, indexName, elementCount, true);

         cacheManager = initializeInfinispan(rootDir, indexName);
         String[] fileNamesFromIndexDir = getFileNamesFromDir(rootDir);

         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         Set keyList = cacheLoader.loadAllKeys(null);

         for(String fileName : fileNamesFromIndexDir) {
            FileCacheKey key = new FileCacheKey(indexName, fileName);
            assert cacheLoader.load(key) != null;

            boolean found = false;
            for(Object keyFromList : keyList) {
               if(keyFromList instanceof FileCacheKey && keyFromList.equals(key)) {
                  found = true;
                  break;
               }
            }

            assert found : "No corresponding key was found for " + key;
         }
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   public void testGetConfigurationClass() {
      File rootDir = createRootDir();
      EmbeddedCacheManager cacheManager = null;
      try {
         cacheManager = initializeInfinispan(rootDir, indexName);
         LuceneCacheLoader cacheLoader = (LuceneCacheLoader) TestingUtil.extractComponent(cacheManager.getCache(),
                                                                                          CacheLoaderManager.class).getCacheLoader();

         assert cacheLoader.getConfigurationClass() == LuceneCacheLoaderConfig.class;
      } finally {
         TestingUtil.recursiveFileRemove(rootDir);

         if(cacheManager != null) {
            TestingUtil.killCacheManagers(cacheManager);
         }
      }
   }

   @DataProvider(name = "passEntriesCount")
   public Object[][] provideEntriesCount() {
      return new Object[][]{
            {new Integer(elementCount + 1)},
            {null}
      };
   }

   @DataProvider(name = "rootDirProvider")
   public Object[][] provideRootDir() {
      File rootDir = createRootDir();
      return new Object[][]{
            {rootDir},
            {new File(new File(parentDir), rootDirectoryName + "___")}
      };
   }

   private String[] getFileNamesFromDir(File rootDir) {
      File indexDir = new File(rootDir, indexName);
      assert indexDir.exists();

      String[] fileNames = indexDir.list();
      assert fileNames.length > 0;

      return fileNames;
   }

}
