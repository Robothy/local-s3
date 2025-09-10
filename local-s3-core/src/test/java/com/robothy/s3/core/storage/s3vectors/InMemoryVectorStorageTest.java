package com.robothy.s3.core.storage.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.TotalSizeExceedException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VectorStorage} in-memory implementation.
 * Tests low-level vector data storage functionality.
 */
class InMemoryVectorStorageTest {

  private VectorStorage vectorStorage;

  @BeforeEach
  void setUp() {
    vectorStorage = VectorStorage.createInMemory();
  }

  @Test
  void testPutAndGetVectorData() {
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertNotNull(storageId);
    assertTrue(storageId > 0);
    
    float[] retrieved = vectorStorage.getVectorData(storageId);
    assertNotNull(retrieved);
    assertArrayEquals(vectorData, retrieved);
  }

  @Test
  void testVectorDataExists() {
    float[] vectorData = {1.0f, 2.0f};
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertTrue(vectorStorage.vectorDataExists(storageId));
    assertFalse(vectorStorage.vectorDataExists(storageId + 1000));
    assertFalse(vectorStorage.vectorDataExists(null));
  }

  @Test
  void testDeleteVectorData() {
    float[] vectorData = {1.0f, 2.0f};
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertTrue(vectorStorage.vectorDataExists(storageId));
    
    boolean deleted = vectorStorage.deleteVectorData(storageId);
    assertTrue(deleted);
    
    assertFalse(vectorStorage.vectorDataExists(storageId));
    assertNull(vectorStorage.getVectorData(storageId));
    
    // Deleting non-existent data should return false
    assertFalse(vectorStorage.deleteVectorData(storageId));
    assertFalse(vectorStorage.deleteVectorData(null));
  }

  @Test
  void testGetStoredVectorCount() {
    assertEquals(0, vectorStorage.getStoredVectorCount());
    
    Long id1 = vectorStorage.putVectorData(new float[]{1.0f});
    assertEquals(1, vectorStorage.getStoredVectorCount());
    
    Long id2 = vectorStorage.putVectorData(new float[]{2.0f});
    assertEquals(2, vectorStorage.getStoredVectorCount());
    
    vectorStorage.deleteVectorData(id1);
    assertEquals(1, vectorStorage.getStoredVectorCount());
    
    vectorStorage.deleteVectorData(id2);
    assertEquals(0, vectorStorage.getStoredVectorCount());
  }

  @Test
  void testGetVectorDataSize() {
    float[] vectorData = {1.0f, 2.0f, 3.0f}; // 3 floats = 12 bytes
    Long storageId = vectorStorage.putVectorData(vectorData);
    
    assertEquals(12, vectorStorage.getVectorDataSize(storageId));
    assertEquals(-1, vectorStorage.getVectorDataSize(storageId + 1000));
    assertEquals(-1, vectorStorage.getVectorDataSize(null));
  }

  @Test
  void testInvalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> 
        vectorStorage.putVectorData(null));
    
    assertThrows(IllegalArgumentException.class, () -> 
        vectorStorage.putVectorData(new float[]{}));
    
    assertThrows(IllegalArgumentException.class, () -> 
        vectorStorage.putVectorData(new float[]{Float.NaN}));
    
    assertThrows(IllegalArgumentException.class, () -> 
        vectorStorage.putVectorData(new float[]{Float.POSITIVE_INFINITY}));
    
    assertThrows(IllegalArgumentException.class, () -> 
        vectorStorage.putVectorData(new float[]{Float.NEGATIVE_INFINITY}));
  }

  @Test
  void testMaxStorageSizeLimit() {
    // Create storage with 10-byte limit (enough for 2 floats)
    VectorStorage limitedStorage = VectorStorage.createInMemory(10);
    
    // First vector should fit (8 bytes)
    Long id1 = limitedStorage.putVectorData(new float[]{1.0f, 2.0f});
    assertNotNull(id1);
    
    // Second vector should exceed limit (would be 12 more bytes)
    assertThrows(TotalSizeExceedException.class, () -> 
        limitedStorage.putVectorData(new float[]{3.0f, 4.0f, 5.0f}));
    
    // After deleting first vector, new vector should fit
    limitedStorage.deleteVectorData(id1);
    assertDoesNotThrow(() -> 
        limitedStorage.putVectorData(new float[]{3.0f, 4.0f}));
  }

  @Test
  void testDefensiveCopy() {
    float[] original = {1.0f, 2.0f, 3.0f};
    Long storageId = vectorStorage.putVectorData(original);
    
    // Modify original array
    original[0] = 999.0f;
    
    // Retrieved data should not be affected
    float[] retrieved = vectorStorage.getVectorData(storageId);
    assertEquals(1.0f, retrieved[0]); // Should still be 1.0f, not 999.0f
    
    // Modify retrieved array
    retrieved[1] = 888.0f;
    
    // Storage should not be affected
    float[] retrievedAgain = vectorStorage.getVectorData(storageId);
    assertEquals(2.0f, retrievedAgain[1]); // Should still be 2.0f, not 888.0f
  }
}
