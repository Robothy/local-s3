package com.robothy.s3.core.assertions.vectors;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.service.s3vectors.S3VectorsMetadataAware;
import com.robothy.s3.core.util.S3VectorsArnUtils;
import org.apache.commons.lang3.StringUtils;

public class VectorBucketAssertions {

  /**
   * Resolve bucket name from either vectorBucketName or vectorBucketArn.
   *
   * @param vectorBucketName the vector bucket name
   * @param vectorBucketArn the vector bucket ARN
   * @return resolved bucket name
   * @throws LocalS3VectorException if both are null or empty
   */
  public static String resolveBucketName(String vectorBucketName, String vectorBucketArn) {
    String bucketName = vectorBucketName;
    if (bucketName == null && vectorBucketArn != null) {
      bucketName = S3VectorsArnUtils.extractBucketNameFromArn(vectorBucketArn);
    }

    if (StringUtils.isBlank(bucketName)) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
          "Vector bucket name is required");
    }

    return bucketName;
  }


  /**
   * Assert that the vector bucket exists.
   *
   * @param metadataAware service that provides metadata access
   * @param bucketName    bucket name to validate
   * @return fetched VectorBucketMetadata instance
   * @throws LocalS3VectorException if bucket doesn't exist
   */
  public static VectorBucketMetadata assertVectorBucketExists(S3VectorsMetadataAware metadataAware, String bucketName) {
    VectorBucketMetadata bucketMetadata = metadataAware.metadata().getVectorBucketMetadataMap().get(bucketName);
    if (bucketMetadata == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND,
          "The specified vector bucket could not be found");
    }
    return bucketMetadata;
  }



}
