package com.robothy.s3.core.exception;

/**
 * Indicates that the give upload ID not eixsts.
 */
public class UploadNotExistException extends LocalS3Exception {

  public UploadNotExistException(String key, String uploadId) {
    super(S3ErrorCode.NoSuchUpload, "key=" + key + ", uploadId=" + uploadId );
  }

}
