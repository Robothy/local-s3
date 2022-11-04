package com.robothy.s3.core.exception;

/**
 * Invalid bucket name exception. Client side error.
 */
public class InvalidBucketNameException extends LocalS3Exception {

  /**
   * Construct an instance.
   *
   * @param bucketName invalid bucket name.
   */
  public InvalidBucketNameException(String bucketName) {
    super(S3ErrorCode.InvalidBucketName, "The bucket name '" + bucketName + "' is invalid.");
  }

}
