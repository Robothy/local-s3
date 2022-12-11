package com.robothy.s3.core.exception;

/**
 * Versioned object not exists. Client side exception.
 */
public class VersionedObjectNotExistException extends LocalS3Exception {

  /**
   * Create a {@linkplain VersionedObjectNotExistException} instance.
   *
   * @param key the object key.
   * @param versionId the version ID.
   */
  public VersionedObjectNotExistException(String key, String versionId) {
    super(S3ErrorCode.NoSuchVersion, "Object(key=" + key + ") hasn't version '" + versionId + "'.");
  }

  /**
   * Create an instance.
   *
   * @param version the object version.
   */
  public VersionedObjectNotExistException(String version) {
    super(S3ErrorCode.NoSuchVersion, "The object doesn't have version ID: " + version);
  }

}
