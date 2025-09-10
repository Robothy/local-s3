package com.robothy.s3.core.storage.s3vectors;

/**
 * Vector data storage abstraction for S3 Vectors operations.
 * This interface only handles the storage of vector data (float arrays) 
 * and returns storage IDs for referencing the stored data.
 */
public interface VectorStorage {

  /**
   * Create an in-memory {@linkplain VectorStorage} implementation.
   * 
   * @return a new in-memory vector storage instance
   */
  static VectorStorage createInMemory() {
    return new InMemoryVectorStorage();
  }

  /**
   * Create an in-memory {@linkplain VectorStorage} implementation with size limit.
   * 
   * @param maxStorageSize maximum storage size in bytes
   * @return a new in-memory vector storage instance
   */
  static VectorStorage createInMemory(long maxStorageSize) {
    return new InMemoryVectorStorage(maxStorageSize);
  }

  /**
   * Create a file system {@linkplain VectorStorage} implementation.
   * 
   * @param storageDirectory the directory to store vector files
   * @param maxCachedVectorCount maximum number of vectors to keep in memory cache
   * @return a new file system vector storage instance
   */
  static VectorStorage createFileSystem(java.nio.file.Path storageDirectory, int maxCachedVectorCount) {
    return new FileSystemVectorStorage(storageDirectory, maxCachedVectorCount);
  }

  /**
   * Store vector data and return a storage ID.
   * Similar to {@link com.robothy.s3.core.storage.Storage#put(byte[])}.
   * 
   * @param vectorData the vector data as float32 array
   * @return a unique storage ID for the stored vector data
   * @throws IllegalArgumentException if vectorData is null or empty
   */
  Long putVectorData(float[] vectorData);

  /**
   * Retrieve vector data by storage ID.
   * 
   * @param storageId the storage ID returned by putVectorData
   * @return the vector data as float32 array, or null if not found
   */
  float[] getVectorData(Long storageId);

  /**
   * Delete vector data by storage ID.
   * 
   * @param storageId the storage ID to delete
   * @return true if the data was deleted, false if not found
   */
  boolean deleteVectorData(Long storageId);

  /**
   * Check if vector data exists for the given storage ID.
   * 
   * @param storageId the storage ID to check
   * @return true if the data exists, false otherwise
   */
  boolean vectorDataExists(Long storageId);

  /**
   * Get the total number of stored vector data entries.
   * 
   * @return the total count of stored vectors
   */
  long getStoredVectorCount();

  /**
   * Get the size in bytes of the vector data for the given storage ID.
   * 
   * @param storageId the storage ID
   * @return the size in bytes, or -1 if not found
   */
  long getVectorDataSize(Long storageId);


}
