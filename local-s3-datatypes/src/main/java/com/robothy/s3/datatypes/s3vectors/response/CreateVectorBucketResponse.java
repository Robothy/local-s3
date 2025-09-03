package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for CreateVectorBucket operation.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_CreateVectorBucket.html">CreateVectorBucket API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVectorBucketResponse {

  /**
   * The name of the created vector bucket.
   */
  @JsonProperty("VectorBucketName")
  private String vectorBucketName;

  /**
   * The Amazon Resource Name (ARN) of the created vector bucket.
   */
  @JsonProperty("VectorBucketArn")
  private String vectorBucketArn;

  /**
   * The creation date and time of the vector bucket in ISO 8601 format.
   */
  @JsonProperty("CreationDate")
  private String creationDate;

}
