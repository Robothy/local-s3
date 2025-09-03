package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for getting vectors from a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetVectors.html">GetVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetVectorsRequest {

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
   * The names of the vectors you want to return attributes for.
   * Array Members: Minimum number of 1 item. Maximum number of 100 items.
   * Length Constraints: Minimum length of 1. Maximum length of 1024.
   * Required: Yes
   */
  @JsonProperty("keys")
  private List<String> keys;

  /**
   * Indicates whether to include the vector data in the response. 
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnData")
  private Boolean returnData;

  /**
   * Indicates whether to include metadata in the response. 
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnMetadata")
  private Boolean returnMetadata;

  /**
   * The name of the vector bucket that contains the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
