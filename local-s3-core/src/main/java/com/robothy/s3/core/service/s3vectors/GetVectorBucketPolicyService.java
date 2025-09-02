package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.datatypes.s3vectors.response.GetVectorBucketPolicyResponse;

public interface GetVectorBucketPolicyService extends S3VectorsMetadataAware {

  default GetVectorBucketPolicyResponse getVectorBucketPolicy(String vectorBucketName) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    String policy = extractPolicyFromBucket(bucketMetadata);
    return buildResponse(policy);
  }

  private String extractPolicyFromBucket(VectorBucketMetadata bucketMetadata) {
    String policy = bucketMetadata.getPolicy().orElse(null);
    if (policy == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND, 
          "The vector bucket policy does not exist");
    }
    return policy;
  }

  private GetVectorBucketPolicyResponse buildResponse(String policy) {
    return GetVectorBucketPolicyResponse.builder()
        .policy(policy)
        .build();
  }
}
