package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.InMemoryBucketService;
import com.robothy.s3.core.service.InMemoryObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.loader.FileSystemS3MetadataLoader;
import com.robothy.s3.core.storage.Storage;
import com.robothy.s3.core.storage.StorageOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * In memory implementation of {@linkplain LocalS3Manager}. Mange in memory
 * local-s3 related services.
 */
class InMemoryLocalS3Manager implements LocalS3Manager {

  private final LocalS3Metadata s3Metadata;

  private final Storage storage;

  InMemoryLocalS3Manager(Path initialDataDirectory) {
    this.s3Metadata = loadS3Metadata(initialDataDirectory);
    this.storage = Storage.create(StorageOptions.builder()
        .inMemory(true)
        .build());
  }

  @Override
  public BucketService bucketService() {
    return InMemoryBucketService.create(s3Metadata);
  }

  @Override
  public ObjectService objectService() {
    return InMemoryObjectService.create(s3Metadata, storage);
  }

  private LocalS3Metadata loadS3Metadata(Path initialDataDirectory) {
    if (Objects.isNull(initialDataDirectory)) {
      return new LocalS3Metadata();
    }

    if (!Files.exists(initialDataDirectory)) {
      throw new IllegalArgumentException(initialDataDirectory.toAbsolutePath() + " not found.");
    }

    return FileSystemS3MetadataLoader.create().load(initialDataDirectory);
  }

}
