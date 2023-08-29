package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.ObjectService;
import java.nio.file.Path;

/**
 * A manager abstraction that manages data and services of local-s3.
 */
public interface LocalS3Manager {

  /**
   * Relative path of storage.
   */
   String STORAGE_DIRECTORY = ".storage";

  /**
   * Create an in-memory implementation of {@linkplain LocalS3Manager}.
   *
   * @return an instance of in-memory implementation.
   */
  static LocalS3Manager createInMemoryS3Manager() {
    return new InMemoryLocalS3Manager(null, false);
  }

  static LocalS3Manager createInMemoryS3Manager(Path dataDirectory, boolean enabledInitialDataCache) {
    return new InMemoryLocalS3Manager(dataDirectory, enabledInitialDataCache);
  }

  /**
   * Create a file system implementation of {@linkplain LocalS3Manager}.
   *
   * @return an instance of file system implementation.
   */
  static LocalS3Manager createFileSystemS3Manager(Path dataDirectory) {
    return new FileSystemLocalS3Manager(dataDirectory);
  }

  /**
   * Get a bucket service.
   *
   * @return a bucket service.
   */
  BucketService bucketService();

  /**
   * Get an object service.
   *
   * @return an object service.
   */
  ObjectService objectService();

}
