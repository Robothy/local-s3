package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for querying vectors using similarity search.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_QueryVectors.html">QueryVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryVectorsRequest {

  /**
   * Metadata filter to apply during the query.
   * Required: No
   */
  @JsonProperty("filter")
  private JsonNode filter;

  /**
   * The ARN of the vector index that you want to query.
   * Required: No
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index that you want to query.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The query vector. Ensure that the query vector has the same dimension 
   * as the dimension of the vector index that's being queried.
   * Required: Yes
   */
  @JsonProperty("queryVector")
  private PutInputVector.VectorData queryVector;

  /**
   * Indicates whether to include the computed distance in the response.
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnDistance")
  private Boolean returnDistance;

  /**
   * Indicates whether to include metadata in the response.
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnMetadata")
  private Boolean returnMetadata;

  /**
   * The number of results to return for each query.
   * Valid Range: Minimum value of 1.
   * Required: Yes
   */
  @JsonProperty("topK")
  private Integer topK;

  /**
   * The name of the vector bucket that contains the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
