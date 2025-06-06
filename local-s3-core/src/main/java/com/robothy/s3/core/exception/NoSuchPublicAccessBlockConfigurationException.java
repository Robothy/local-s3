package com.robothy.s3.core.exception;

/**
 * Thrown when attempting to access a public access block configuration that doesn't exist.
 */
public class NoSuchPublicAccessBlockConfigurationException extends LocalS3Exception {

  /**
   * Construct a {@linkplain NoSuchPublicAccessBlockConfigurationException} instance.
   *
   * @param bucketName name of the bucket.
   */
  public NoSuchPublicAccessBlockConfigurationException(String bucketName) {
    super(S3ErrorCode.NoSuchPublicAccessBlockConfiguration, 
        "The public access block configuration was not found for bucket: " + bucketName);
  }
}
