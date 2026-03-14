package com.robothy.s3.core.exception;

/**
 * Thrown when the requested byte range cannot be satisfied.
 */
public class InvalidRangeException extends LocalS3Exception {

  public InvalidRangeException() {
    super(S3ErrorCode.InvalidRange);
  }

}
