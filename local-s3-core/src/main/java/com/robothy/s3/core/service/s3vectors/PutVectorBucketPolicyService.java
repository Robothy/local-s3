package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.PutVectorBucketPolicyResponse;

public interface PutVectorBucketPolicyService extends S3VectorsMetadataAware {

  @BucketChanged
  default PutVectorBucketPolicyResponse putVectorBucketPolicy(String vectorBucketName, String policy) {
    validatePolicyDocument(policy);
    VectorBucketMetadata bucketMetadata = getBucketMetadata(vectorBucketName);
    storePolicyInBucket(bucketMetadata, policy);
    return buildResponse();
  }

  private void validatePolicyDocument(String policy) {
    ValidationUtils.validateNotBlank(policy, "Policy document is required");
    
    String trimmedPolicy = policy.trim();
    if (!trimmedPolicy.startsWith("{") || !trimmedPolicy.endsWith("}")) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Policy document must be valid JSON");
    }
  }

  private VectorBucketMetadata getBucketMetadata(String bucketName) {
    VectorBucketMetadata bucketMetadata = metadata().getVectorBucketMetadataMap().get(bucketName);
    if (bucketMetadata == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND,
          "The specified vector bucket could not be found: " + bucketName);
    }
    return bucketMetadata;
  }

  private void storePolicyInBucket(VectorBucketMetadata bucketMetadata, String policy) {
    bucketMetadata.setPolicy(policy);
  }

  private PutVectorBucketPolicyResponse buildResponse() {
    return PutVectorBucketPolicyResponse.builder().build();
  }
}
