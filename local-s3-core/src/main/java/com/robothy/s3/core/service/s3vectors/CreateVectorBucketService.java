package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.utils.s3vectors.DateTimeUtils;
import com.robothy.s3.datatypes.s3vectors.EncryptionConfiguration;
import com.robothy.s3.datatypes.s3vectors.VectorBucket;
import com.robothy.s3.datatypes.s3vectors.response.CreateVectorBucketResponse;

public interface CreateVectorBucketService extends S3VectorsMetadataAware {

  @BucketChanged(type = BucketChanged.Type.CREATE)
  default CreateVectorBucketResponse createVectorBucket(String bucketName, EncryptionConfiguration encryptionConfiguration) {
    validateBucketName(bucketName);
    assertBucketDoesNotExist(bucketName);
    VectorBucketMetadata bucketMetadata = createBucketMetadata(bucketName, encryptionConfiguration);
    storeBucketMetadata(bucketMetadata);
    return buildResponse(bucketMetadata);
  }

  private static void validateBucketName(String bucketName) {
    if (bucketName == null || bucketName.trim().isEmpty()) {
      throw new LocalS3VectorException(
          LocalS3VectorErrorType.INVALID_REQUEST,
          "Vector bucket name is required"
      );
    }
    // Reuse existing bucket validation from LocalS3
    BucketAssertions.assertBucketNameIsValid(bucketName);
  }

  private void assertBucketDoesNotExist(String bucketName) {
    if (metadata().getVectorBucketMetadataMap().containsKey(bucketName)) {
      throw new BucketAlreadyExistsException("Vector bucket already exists: " + bucketName);
    }
  }

  private VectorBucketMetadata createBucketMetadata(String bucketName, EncryptionConfiguration encryptionConfiguration) {
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName(bucketName);
    bucketMetadata.setCreationDate(DateTimeUtils.getCurrentTimestamp());
    bucketMetadata.setEncryptionConfiguration(encryptionConfiguration);
    return bucketMetadata;
  }

  private void storeBucketMetadata(VectorBucketMetadata bucketMetadata) {
    metadata().addVectorBucketMetadata(bucketMetadata);
  }

  private CreateVectorBucketResponse buildResponse(VectorBucketMetadata bucketMetadata) {
    return CreateVectorBucketResponse.builder()
        .vectorBucketName(bucketMetadata.getVectorBucketName())
        .vectorBucketArn(VectorBucket.generateArn(bucketMetadata.getVectorBucketName()))
        .creationDate(DateTimeUtils.formatTimestamp(bucketMetadata.getCreationDate()))
        .build();
  }

}
