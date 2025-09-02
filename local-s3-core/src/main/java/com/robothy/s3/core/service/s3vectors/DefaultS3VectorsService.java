package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;

/**
 * Default implementation of {@link S3VectorsService}.
 * All S3 vectors data is managed through the provided metadata instance.
 */
final class DefaultS3VectorsService implements S3VectorsService, S3VectorsStorageAware {

  private final LocalS3VectorsMetadata localS3VectorsMetadata;

  private final VectorStorage vectorStorage;

  public DefaultS3VectorsService(LocalS3VectorsMetadata localS3VectorsMetadata, VectorStorage vectorStorage) {
    this.localS3VectorsMetadata = localS3VectorsMetadata;
    this.vectorStorage = vectorStorage;
  }

  @Override
  public LocalS3VectorsMetadata metadata() {
    return localS3VectorsMetadata;
  }

  @Override
  public VectorStorage vectorStorage() {
    return this.vectorStorage;
  }
}
