package com.robothy.s3.datatypes.s3vectors;

import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents an S3 Vector Bucket following AWS S3 Vectors API specification.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateVectorBucket.html">CreateVectorBucket API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VectorBucket {

  /**
   * The name of the vector bucket.
   * Must follow S3 bucket naming conventions.
   */
  private String vectorBucketName;

  /**
   * The Amazon Resource Name (ARN) of the vector bucket.
   * Format: arn:aws:s3vectors:::vector-bucket/{name}
   */
  private String arn;

  /**
   * The creation time of the vector bucket.
   */
  private Instant creationTime;

  /**
   * Optional encryption configuration for the vector bucket.
   * If null, the bucket uses default encryption settings.
   */
  private EncryptionConfiguration encryptionConfiguration;

  /**
   * Get the encryption configuration for this vector bucket.
   * 
   * @return Optional encryption configuration
   */
  public Optional<EncryptionConfiguration> getEncryptionConfiguration() {
    return Optional.ofNullable(encryptionConfiguration);
  }

  /**
   * Generate the ARN for a vector bucket name.
   * 
   * @param bucketName the name of the vector bucket
   * @return the ARN string
   */
  public static String generateArn(String bucketName) {
    return String.format("arn:aws:s3vectors:::vector-bucket/%s", bucketName);
  }
}
