package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for retrieving a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetIndex.html">GetIndex API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetIndexRequest {

  /**
   * The ARN of the vector index.
   * Required: No
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The name of the vector bucket that contains the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
