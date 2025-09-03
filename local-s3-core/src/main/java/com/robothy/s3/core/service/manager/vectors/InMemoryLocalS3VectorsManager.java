package com.robothy.s3.core.service.manager.vectors;

import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;

final class InMemoryLocalS3VectorsManager implements LocalS3VectorsManager {

  @Override
  public S3VectorsService s3VectorsService() {
    LocalS3VectorsMetadata vectorsMetadata = new LocalS3VectorsMetadata();
    VectorStorage storage = VectorStorage.createInMemory();
    return S3VectorsService.create(vectorsMetadata, storage);
  }

}
