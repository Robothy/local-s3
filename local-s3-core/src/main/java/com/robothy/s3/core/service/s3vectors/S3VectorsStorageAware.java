package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.storage.s3vectors.VectorStorage;

public interface S3VectorsStorageAware {

  VectorStorage vectorStorage();

}
