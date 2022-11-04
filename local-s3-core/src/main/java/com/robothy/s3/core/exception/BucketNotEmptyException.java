package com.robothy.s3.core.exception;

public class BucketNotEmptyException extends LocalS3Exception {

  public BucketNotEmptyException(String bucketName) {
    super(S3ErrorCode.BucketNotEmpty, "The bucket '" + bucketName + "' is not empty.");
  }

}
