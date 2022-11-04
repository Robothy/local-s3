package com.robothy.s3.core.exception;

public class BucketTaggingNotExistException extends LocalS3Exception {

  public BucketTaggingNotExistException(String bucketName) {
    super(S3ErrorCode.NoSuchTagSet, "The bucket tagging not set.");
  }

}
