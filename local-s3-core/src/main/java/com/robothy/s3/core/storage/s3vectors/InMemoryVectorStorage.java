package com.robothy.s3.core.storage.s3vectors;

import com.robothy.s3.core.exception.TotalSizeExceedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory implementation of {@linkplain VectorStorage}.
 * Stores vector data in Java heap memory using concurrent data structures.
 * This implementation only handles raw vector data storage and retrieval.
 */
@Slf4j
class InMemoryVectorStorage implements VectorStorage {

  private final Map<Long, float[]> vectorStore = new ConcurrentHashMap<>();
  private final AtomicLong nextStorageId = new AtomicLong(1);
  private final AtomicLong totalSize = new AtomicLong(0);
  private final long maxStorageSize;

  /**
   * Create an {@linkplain InMemoryVectorStorage} without size limitation.
   */
  InMemoryVectorStorage() {
    this.maxStorageSize = Long.MAX_VALUE;
  }

  /**
   * Create an {@linkplain InMemoryVectorStorage} with maximum storage size limit.
   * 
   * @param maxStorageSize maximum storage size in bytes
   */
  InMemoryVectorStorage(long maxStorageSize) {
    this.maxStorageSize = maxStorageSize;
  }

  @Override
  public Long putVectorData(float[] vectorData) {
    if (vectorData == null || vectorData.length == 0) {
      throw new IllegalArgumentException("Vector data cannot be null or empty");
    }

    // Validate float32 values
    for (int i = 0; i < vectorData.length; i++) {
      float value = vectorData[i];
      if (!Float.isFinite(value)) {
        throw new IllegalArgumentException("Vector data contains invalid value at index " + i + ": " + value);
      }
    }

    // Calculate storage size (4 bytes per float)
    long vectorSize = vectorData.length * 4L;
    
    // Check size limit
    if (totalSize.get() + vectorSize > maxStorageSize) {
      throw new TotalSizeExceedException(maxStorageSize, totalSize.get() + vectorSize);
    }

    // Generate storage ID and store vector data (defensive copy)
    Long storageId = nextStorageId.getAndIncrement();
    float[] vectorCopy = Arrays.copyOf(vectorData, vectorData.length);
    vectorStore.put(storageId, vectorCopy);
    totalSize.addAndGet(vectorSize);
    
    log.debug("Stored vector data with storage ID: {}, size: {} bytes", storageId, vectorSize);
    return storageId;
  }

  @Override
  public float[] getVectorData(Long storageId) {
    if (storageId == null) {
      return null;
    }
    
    float[] vectorData = vectorStore.get(storageId);
    // Return defensive copy to prevent external modifications
    return vectorData != null ? Arrays.copyOf(vectorData, vectorData.length) : null;
  }

  @Override
  public boolean deleteVectorData(Long storageId) {
    if (storageId == null) {
      return false;
    }
    
    float[] removedData = vectorStore.remove(storageId);
    if (removedData != null) {
      long vectorSize = removedData.length * 4L;
      totalSize.addAndGet(-vectorSize);
      log.debug("Deleted vector data with storage ID: {}, freed {} bytes", storageId, vectorSize);
      return true;
    }
    return false;
  }

  @Override
  public boolean vectorDataExists(Long storageId) {
    return storageId != null && vectorStore.containsKey(storageId);
  }

  @Override
  public long getStoredVectorCount() {
    return vectorStore.size();
  }


  @Override
  public long getVectorDataSize(Long storageId) {
    if (storageId == null) {
      return -1;
    }
    
    float[] vectorData = vectorStore.get(storageId);
    return vectorData != null ? vectorData.length * 4L : -1;
  }
}
