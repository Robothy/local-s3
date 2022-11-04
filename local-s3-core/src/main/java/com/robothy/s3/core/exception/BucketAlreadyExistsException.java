package com.robothy.s3.core.exception;

public class BucketAlreadyExistsException extends LocalS3Exception {

  public BucketAlreadyExistsException(String bucketName) {
    super(S3ErrorCode.BucketAlreadyExists, "The bucket '" + bucketName + "' already exist.");
  }

}
