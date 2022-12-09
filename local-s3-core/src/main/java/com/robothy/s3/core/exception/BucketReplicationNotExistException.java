package com.robothy.s3.core.exception;

/**
 * Bucket replication configuration not exists.
 */
public class BucketReplicationNotExistException extends LocalS3Exception {

  /**
   * Create a {@linkplain BucketReplicationNotExistException} instance.
   */
  public BucketReplicationNotExistException() {
    super(S3ErrorCode.ReplicationConfigurationNotFoundError);
  }

}
