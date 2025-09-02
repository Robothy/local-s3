package com.robothy.s3.core.storage.s3vectors;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemVectorStorageTest {

  @TempDir
  Path tempDir;

  private VectorStorage vectorStorage;

  @BeforeEach
  void setUp() {
    vectorStorage = VectorStorage.createFileSystem(tempDir, 3); // Cache size of 3
  }

  @Test
  void testPutAndGetVectorData() {
    float[] vectorData = {1.0f, 2.0f, 3.0f, 4.0f};
    
    Long storageId = vectorStorage.putVectorData(vectorData);
    assertNotNull(storageId);
    assertTrue(storageId > 0);
    
    float[] retrievedData = vectorStorage.getVectorData(storageId);
    assertArrayEquals(vectorData, retrievedData);
  }

  @Test
  void testPutVectorDataCreatesFile() throws IOException {
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    Path expectedFile = tempDir.resolve(String.valueOf(storageId));
    assertTrue(Files.exists(expectedFile));
    assertTrue(Files.isRegularFile(expectedFile));
  }

  @Test
  void testGetNonExistentVector() {
    float[] result = vectorStorage.getVectorData(999L);
    assertNull(result);
  }

  @Test
  void testGetNullStorageId() {
    float[] result = vectorStorage.getVectorData(null);
    assertNull(result);
  }

  @Test
  void testPutNullVectorData() {
    assertThrows(IllegalArgumentException.class, () -> {
      vectorStorage.putVectorData(null);
    });
  }

  @Test
  void testPutEmptyVectorData() {
    assertThrows(IllegalArgumentException.class, () -> {
      vectorStorage.putVectorData(new float[0]);
    });
  }

  @Test
  void testPutInvalidVectorData() {
    float[] invalidData = {1.0f, Float.NaN, 3.0f};
    assertThrows(IllegalArgumentException.class, () -> {
      vectorStorage.putVectorData(invalidData);
    });
    
    float[] infiniteData = {1.0f, Float.POSITIVE_INFINITY, 3.0f};
    assertThrows(IllegalArgumentException.class, () -> {
      vectorStorage.putVectorData(infiniteData);
    });
  }

  @Test
  void testDeleteVectorData() {
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertTrue(vectorStorage.vectorDataExists(storageId));
    
    boolean deleted = vectorStorage.deleteVectorData(storageId);
    assertTrue(deleted);
    
    assertFalse(vectorStorage.vectorDataExists(storageId));
    assertNull(vectorStorage.getVectorData(storageId));
  }

  @Test
  void testDeleteNonExistentVector() {
    boolean deleted = vectorStorage.deleteVectorData(999L);
    assertFalse(deleted);
  }

  @Test
  void testDeleteNullStorageId() {
    boolean deleted = vectorStorage.deleteVectorData(null);
    assertFalse(deleted);
  }

  @Test
  void testVectorDataExists() {
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertTrue(vectorStorage.vectorDataExists(storageId));
    assertFalse(vectorStorage.vectorDataExists(999L));
    assertFalse(vectorStorage.vectorDataExists(null));
  }

  @Test
  void testGetStoredVectorCount() {
    assertEquals(0, vectorStorage.getStoredVectorCount());

    Long vector1Id = vectorStorage.putVectorData(new float[] {1.0f, 2.0f});
    assertEquals(1, vectorStorage.getStoredVectorCount());
    
    vectorStorage.putVectorData(new float[]{3.0f, 4.0f});
    assertEquals(2, vectorStorage.getStoredVectorCount());
    
    vectorStorage.deleteVectorData(vector1Id);
    assertEquals(1, vectorStorage.getStoredVectorCount());
  }

  @Test
  void testGetVectorDataSize() {
    float[] vectorData = {1.0f, 2.0f, 3.0f, 4.0f}; // 4 floats = 16 bytes
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertEquals(16L, vectorStorage.getVectorDataSize(storageId));
    assertEquals(-1L, vectorStorage.getVectorDataSize(999L));
    assertEquals(-1L, vectorStorage.getVectorDataSize(null));
  }

  @Test
  void testLRUCacheEviction() {
    FileSystemVectorStorage fsStorage = (FileSystemVectorStorage) vectorStorage;
    
    // Add 5 vectors to exceed cache size of 3
    Long id1 = vectorStorage.putVectorData(new float[]{1.0f});
    Long id2 = vectorStorage.putVectorData(new float[]{2.0f});
    Long id3 = vectorStorage.putVectorData(new float[]{3.0f});
    Long id4 = vectorStorage.putVectorData(new float[]{4.0f});
    Long id5 = vectorStorage.putVectorData(new float[]{5.0f});
    
    // Cache should contain at most 3 vectors
    assertEquals(3, fsStorage.getCachedVectorCount());
    
    // Access id1 to make it recently used
    vectorStorage.getVectorData(id1);
    
    // Add another vector
    vectorStorage.putVectorData(new float[]{6.0f});
    
    // Cache should still be 3
    assertEquals(3, fsStorage.getCachedVectorCount());
  }

  @Test
  void testCacheHitAndMiss() {
    FileSystemVectorStorage fsStorage = (FileSystemVectorStorage) vectorStorage;
    
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    // First access should be from cache (put operation caches the data)
    float[] result1 = vectorStorage.getVectorData(storageId);
    assertArrayEquals(vectorData, result1);
    
    // Clear cache to force file system read
    fsStorage.clearCache();
    assertEquals(0, fsStorage.getCachedVectorCount());
    
    // Second access should load from file system
    float[] result2 = vectorStorage.getVectorData(storageId);
    assertArrayEquals(vectorData, result2);
    
    // Now it should be in cache again
    assertEquals(1, fsStorage.getCachedVectorCount());
  }

  @Test
  void testConcurrentAccess() throws Exception {
    int threadCount = 10;
    int vectorsPerThread = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    
    List<Future<List<Long>>> futures = new ArrayList<>();
    
    // Submit tasks to store vectors concurrently
    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      Future<List<Long>> future = executor.submit(() -> {
        List<Long> storageIds = new ArrayList<>();
        for (int i = 0; i < vectorsPerThread; i++) {
          float[] vectorData = {threadId + i * 0.1f, threadId + i * 0.2f};
          Long storageId = vectorStorage.putVectorData(vectorData);
          storageIds.add(storageId);
        }
        return storageIds;
      });
      futures.add(future);
    }
    
    // Collect all storage IDs
    List<Long> allStorageIds = new ArrayList<>();
    for (Future<List<Long>> future : futures) {
      allStorageIds.addAll(future.get());
    }
    
    executor.shutdown();
    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    
    // Verify all vectors were stored
    assertEquals(threadCount * vectorsPerThread, allStorageIds.size());
    assertEquals(threadCount * vectorsPerThread, vectorStorage.getStoredVectorCount());
    
    // Verify all vectors can be retrieved
    for (Long storageId : allStorageIds) {
      float[] vectorData = vectorStorage.getVectorData(storageId);
      assertNotNull(vectorData);
      assertEquals(2, vectorData.length);
    }
  }

  @Test
  void testPersistenceAcrossInstances() {
    // Store vectors with first instance
    Long id1 = vectorStorage.putVectorData(new float[]{1.0f, 2.0f});
    Long id2 = vectorStorage.putVectorData(new float[]{3.0f, 4.0f});
    
    // Create new instance with same directory
    VectorStorage newStorage = VectorStorage.createFileSystem(tempDir, 2);
    
    // Verify vectors are still accessible
    assertArrayEquals(new float[]{1.0f, 2.0f}, newStorage.getVectorData(id1));
    assertArrayEquals(new float[]{3.0f, 4.0f}, newStorage.getVectorData(id2));
    assertEquals(2, newStorage.getStoredVectorCount());
  }

  @Test
  void testZeroCacheSize() {
    VectorStorage noCacheStorage = VectorStorage.createFileSystem(tempDir, 0);
    FileSystemVectorStorage fsStorage = (FileSystemVectorStorage) noCacheStorage;
    
    Long storageId = noCacheStorage.putVectorData(new float[]{1.0f, 2.0f});
    
    // Should not cache anything
    assertEquals(0, fsStorage.getCachedVectorCount());
    
    // Should still be able to retrieve data from file
    float[] vectorData = noCacheStorage.getVectorData(storageId);
    assertArrayEquals(new float[]{1.0f, 2.0f}, vectorData);
    
    // Still no cache
    assertEquals(0, fsStorage.getCachedVectorCount());
  }

  @Test
  void testLargeVectorData() {
    // Test with a large vector (10,000 dimensions)
    float[] largeVector = new float[10000];
    for (int i = 0; i < largeVector.length; i++) {
      largeVector[i] = i * 0.001f;
    }
    
    Long storageId = vectorStorage.putVectorData(largeVector);
    float[] retrievedVector = vectorStorage.getVectorData(storageId);
    
    assertArrayEquals(largeVector, retrievedVector);
    assertEquals(40000L, vectorStorage.getVectorDataSize(storageId)); // 10,000 * 4 bytes
  }
}
