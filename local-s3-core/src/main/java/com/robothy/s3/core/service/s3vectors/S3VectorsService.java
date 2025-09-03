package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;

public interface S3VectorsService extends
    CreateVectorBucketService,
    GetVectorBucketService,
    DeleteVectorBucketService,
    ListVectorBucketsService,
    PutVectorBucketPolicyService,
    GetVectorBucketPolicyService,
    DeleteVectorBucketPolicyService,
    CreateIndexService,
    GetIndexService,
    ListIndexesService,
    DeleteIndexService,
    PutVectorsService,
    GetVectorsService,
    DeleteVectorsService,
    QueryVectorsService,
    ListVectorsService {

  static S3VectorsService create(LocalS3VectorsMetadata metadata, VectorStorage vectorStorage) {
    return new DefaultS3VectorsService(metadata, vectorStorage);
  }

}