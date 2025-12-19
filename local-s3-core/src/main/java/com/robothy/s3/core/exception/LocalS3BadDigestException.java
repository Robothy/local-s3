package com.robothy.s3.core.exception;

public class LocalS3BadDigestException extends LocalS3Exception {

  public LocalS3BadDigestException(String message) {
    super(S3ErrorCode.BadDigest, message);
  }

  public LocalS3BadDigestException() {
    super(S3ErrorCode.BadDigest);
  }

}
