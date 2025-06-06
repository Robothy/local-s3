package com.robothy.s3.core.exception;

/**
 * Thrown when attempting to access a public access block configuration that doesn't exist.
 */
public class PublicAccessBlockConfigurationNotExistException extends LocalS3Exception {

  /**
   * Construct a {@linkplain PublicAccessBlockConfigurationNotExistException} instance.
   *
   * @param bucketName name of the bucket.
   */
  public PublicAccessBlockConfigurationNotExistException(String bucketName) {
    super(S3ErrorCode.NoSuchPublicAccessBlockConfiguration, 
        "The public access block configuration was not found for bucket: " + bucketName);
  }
}
