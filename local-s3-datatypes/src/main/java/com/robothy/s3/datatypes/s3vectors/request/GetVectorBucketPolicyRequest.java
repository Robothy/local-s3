package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for GetVectorBucketPolicy operation.
 * Gets details about a vector bucket policy. To specify the bucket, you must use
 * either the vector bucket name or the vector bucket Amazon Resource Name (ARN).
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetVectorBucketPolicy.html">GetVectorBucketPolicy API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetVectorBucketPolicyRequest {

  /**
   * The ARN of the vector bucket.
   * Type: String
   * Required: No
   */
  @JsonProperty("vectorBucketArn")
  private String vectorBucketArn;

  /**
   * The name of the vector bucket.
   * Type: String
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
