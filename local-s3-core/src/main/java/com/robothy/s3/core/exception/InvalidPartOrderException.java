package com.robothy.s3.core.exception;

/**
 * Throws if the provided part numbers is not in ascending order when completing a multipart upload.
 */
public class InvalidPartOrderException extends LocalS3Exception {

  /**
   * Constructor.
   */
  public InvalidPartOrderException() {
    super(S3ErrorCode.InvalidPartOrder);
  }

}
