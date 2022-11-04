package com.robothy.s3.core.exception;

public class BucketPolicyNotExistException extends LocalS3Exception {

  public BucketPolicyNotExistException(String bucketName) {
    super(S3ErrorCode.NoSuchBucketPolicy, "The bucket policy not exist.");
  }

}
