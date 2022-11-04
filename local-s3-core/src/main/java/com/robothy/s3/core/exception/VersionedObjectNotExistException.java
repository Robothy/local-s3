package com.robothy.s3.core.exception;

/**
 * Versioned object not exists. Client side exception.
 */
public class VersionedObjectNotExistException extends LocalS3Exception {

  public VersionedObjectNotExistException(String key, String versionId) {
    super(S3ErrorCode.NoSuchVersion, "Object(key=" + key + ") hasn't version '" + versionId + "'.");
  }

}
