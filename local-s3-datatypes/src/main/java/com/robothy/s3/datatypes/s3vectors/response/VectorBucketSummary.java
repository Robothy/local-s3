package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary information about a vector bucket.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_VectorBucketSummary.html">VectorBucketSummary API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorBucketSummary {

  /**
   * Date and time when the vector bucket was created.
   * Type: Timestamp
   * Required: Yes
   */
  @JsonProperty("creationTime")
  private Long creationTime;

  /**
   * The Amazon Resource Name (ARN) of the vector bucket.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("vectorBucketArn")
  private String vectorBucketArn;

  /**
   * The name of the vector bucket.
   * Type: String
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: Yes
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
