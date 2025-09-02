package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.datatypes.s3vectors.response.DeleteVectorBucketPolicyResponse;

public interface DeleteVectorBucketPolicyService extends S3VectorsMetadataAware {

  @BucketChanged
  default DeleteVectorBucketPolicyResponse deleteVectorBucketPolicy(String vectorBucketName) {
    VectorBucketMetadata bucketMetadata = getBucketMetadata(vectorBucketName);
    removePolicyFromBucket(bucketMetadata);
    return buildResponse();
  }


  private VectorBucketMetadata getBucketMetadata(String bucketName) {
    VectorBucketMetadata bucketMetadata = metadata().getVectorBucketMetadataMap().get(bucketName);
    if (bucketMetadata == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND,
          "The specified vector bucket could not be found: " + bucketName);
    }
    return bucketMetadata;
  }

  private void removePolicyFromBucket(VectorBucketMetadata bucketMetadata) {
    bucketMetadata.setPolicy(null);
  }

  private DeleteVectorBucketPolicyResponse buildResponse() {
    return DeleteVectorBucketPolicyResponse.builder().build();
  }
}
