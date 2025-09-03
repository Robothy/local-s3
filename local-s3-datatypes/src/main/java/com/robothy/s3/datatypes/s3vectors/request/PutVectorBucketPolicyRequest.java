package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for PutVectorBucketPolicy operation.
 * Creates a bucket policy for a vector bucket. To specify the bucket, you must use
 * either the vector bucket name or the vector bucket Amazon Resource Name (ARN).
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_PutVectorBucketPolicy.html">PutVectorBucketPolicy API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PutVectorBucketPolicyRequest {

  /**
   * The JSON that defines the policy. For more information about bucket policies for S3
   * Vectors, see Managing vector bucket policies in the Amazon S3 User Guide.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("policy")
  private String policy;

  /**
   * The Amazon Resource Name (ARN) of the vector bucket.
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
