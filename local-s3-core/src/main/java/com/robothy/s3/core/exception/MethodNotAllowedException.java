package com.robothy.s3.core.exception;

public class MethodNotAllowedException extends LocalS3Exception {

  public MethodNotAllowedException(String msg) {
    super(S3ErrorCode.MethodNotAllowed, msg);
  }

}
