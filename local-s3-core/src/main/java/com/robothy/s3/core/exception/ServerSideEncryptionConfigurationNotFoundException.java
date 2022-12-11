package com.robothy.s3.core.exception;

/**
 * Server side encryption not configured.
 */
public class ServerSideEncryptionConfigurationNotFoundException extends LocalS3Exception {

  /**
   * Constructor.
   *
   * @param bucketName the bucket that not configured server side encryption.
   */
  public ServerSideEncryptionConfigurationNotFoundException(String bucketName) {
    super(bucketName, S3ErrorCode.ServerSideEncryptionConfigurationNotFoundError);
  }

}
