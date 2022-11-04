package com.robothy.s3.core.exception;

/**
 * Represents exceptions comes from LocalS3.
 */
public abstract class LocalS3Exception extends RuntimeException {

  private final S3ErrorCode s3ErrorCode;

  LocalS3Exception(S3ErrorCode s3ErrorCode, String message, Throwable cause) {
    super(message, cause);
    this.s3ErrorCode = s3ErrorCode;
  }

  LocalS3Exception(S3ErrorCode s3ErrorCode, Throwable cause) {
    this(s3ErrorCode, s3ErrorCode.description(), cause);
  }

  LocalS3Exception(S3ErrorCode s3ErrorCode) {
    this(s3ErrorCode, s3ErrorCode.description());
  }

  LocalS3Exception(S3ErrorCode s3ErrorCode, String message) {
    this(s3ErrorCode, message, null);
  }

  public S3ErrorCode getS3ErrorCode() {
    return s3ErrorCode;
  }

}
