package com.robothy.s3.core.exception;

/**
 * Object key is invalid exception. Client side exception.
 */
public class InvalidObjectKeyException extends LocalS3Exception {

  public InvalidObjectKeyException(String key) {
    super(S3ErrorCode.InvalidArgument, "Invalid object key '" + key + "'.");
  }

}
