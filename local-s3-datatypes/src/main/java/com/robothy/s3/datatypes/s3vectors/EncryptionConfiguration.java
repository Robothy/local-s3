package com.robothy.s3.datatypes.s3vectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Encryption configuration for S3 Vector Buckets.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateVectorBucket.html">CreateVectorBucket API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EncryptionConfiguration {

  /**
   * The server-side encryption algorithm used when storing vectors in the bucket.
   * Typically "AES256" or "aws:kms".
   */
  private String sseAlgorithm;

  /**
   * The AWS KMS key ID to use for encryption.
   * Only required when sseAlgorithm is "aws:kms".
   */
  private String kmsKeyId;

  /**
   * Whether bucket key encryption is enabled.
   * When enabled, S3 uses a bucket-level key for KMS encryption.
   */
  private Boolean bucketKeyEnabled;
}
