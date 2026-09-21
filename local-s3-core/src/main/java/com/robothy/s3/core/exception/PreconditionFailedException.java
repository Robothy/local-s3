package com.robothy.s3.core.exception;

/**
 * Thrown when a conditional request header, such as {@code If-Match} or
 * {@code If-None-Match}, does not hold.
 */
public class PreconditionFailedException extends LocalS3Exception {

  public PreconditionFailedException(String message) {
    super(S3ErrorCode.PreconditionFailed, message);
  }

}
