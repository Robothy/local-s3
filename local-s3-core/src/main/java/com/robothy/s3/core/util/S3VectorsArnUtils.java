package com.robothy.s3.core.util;

import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;

/**
 * Utility class for handling S3 Vectors ARNs.
 */
public class S3VectorsArnUtils {

  /**
   * Resolve vector bucket name from either bucketName or bucketArn.
   * Follows the priority: bucketName takes precedence over bucketArn.
   * 
   * @param bucketName the vector bucket name (optional)
   * @param bucketArn the vector bucket ARN (optional)
   * @return resolved bucket name
   * @throws LocalS3VectorException if both parameters are null/empty or ARN format is invalid
   */
  public static String resolveBucketName(String bucketName, String bucketArn) {
    if (bucketName != null && !bucketName.trim().isEmpty()) {
      return bucketName.trim();
    }
    
    if (bucketArn != null && !bucketArn.trim().isEmpty()) {
      return extractBucketNameFromArn(bucketArn);
    }
    
    throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
        "Either vectorBucketName or vectorBucketArn must be provided");
  }

  /**
   * Extract bucket name from vector bucket ARN.
   * ARN format: arn:aws:s3vectors:::vector-bucket/{bucketName}
   */
  public static String extractBucketNameFromArn(String arn) {
    if (arn == null || !arn.startsWith("arn:aws:s3vectors:::vector-bucket/")) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
          "Invalid vector bucket ARN format");
    }
    return arn.substring("arn:aws:s3vectors:::vector-bucket/".length());
  }

  /**
   * Extract bucket name and index name from index ARN.
   * ARN format: arn:aws:s3vectors:::vector-bucket/{bucketName}/index/{indexName}
   * 
   * @return Array with [bucketName, indexName]
   */
  public static String[] extractFromIndexArn(String arn) {
    if (arn == null || !arn.startsWith("arn:aws:s3vectors:::vector-bucket/")) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
          "Invalid index ARN format");
    }
    
    String path = arn.substring("arn:aws:s3vectors:::vector-bucket/".length());
    String[] parts = path.split("/index/");
    if (parts.length != 2) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
          "Invalid index ARN format");
    }
    
    return new String[]{parts[0], parts[1]};
  }
}
