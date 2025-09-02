package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary information about a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_IndexSummary.html">IndexSummary API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexSummary {

  /**
   * Date and time when the vector index was created.
   * Type: Timestamp
   * Required: Yes
   */
  @JsonProperty("creationTime")
  private Long creationTime;

  /**
   * The Amazon Resource Name (ARN) of the vector index.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index.
   * Type: String
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: Yes
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The name of the vector bucket that contains the vector index.
   * Type: String
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: Yes
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
