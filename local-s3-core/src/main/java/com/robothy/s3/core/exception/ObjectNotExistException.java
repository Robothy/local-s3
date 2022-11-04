package com.robothy.s3.core.exception;

/**
 * Object not exists exception. Client side exception.
 */
public class ObjectNotExistException extends LocalS3Exception {

  public ObjectNotExistException(String key) {
    super(S3ErrorCode.NoSuchKey, "Object key '" + key + "' not exists.");
  }

}
