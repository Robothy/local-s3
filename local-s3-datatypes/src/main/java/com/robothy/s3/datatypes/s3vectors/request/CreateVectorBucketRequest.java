package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robothy.s3.datatypes.s3vectors.EncryptionConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for CreateVectorBucket operation.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_CreateVectorBucket.html">CreateVectorBucket API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVectorBucketRequest {

  /**
   * The name of the vector bucket to create.
   * Must follow S3 bucket naming conventions.
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

  /**
   * Optional encryption configuration for the vector bucket.
   * If not provided, the bucket will use default encryption settings.
   */
  @JsonProperty("encryptionConfiguration")
  private EncryptionConfiguration encryptionConfiguration;

}
