package com.robothy.s3.core.exception;

/**
 * Bucket not exist exception. Client side exception.
 */
public class BucketNotExistException extends LocalS3Exception {

  /**
   * Construct a {@linkplain BucketNotEmptyException} instance.
   *
   * @param bucketName inputted bucket name that is invalid.
   */
  public BucketNotExistException(String bucketName) {
    super(S3ErrorCode.NoSuchBucket, "Bucket '" + bucketName + "' not exist.");
  }

}
