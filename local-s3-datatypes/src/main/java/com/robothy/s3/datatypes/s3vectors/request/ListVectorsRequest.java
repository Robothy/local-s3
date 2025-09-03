package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for listing vectors in a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListVectors.html">ListVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVectorsRequest {

  /**
   * The name of the vector bucket.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No (but either this + indexName or indexArn must be provided)
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

  /**
   * The Amazon Resource Name (ARN) of the vector index.
   * Required: No (but either this or vectorBucketName + indexName must be provided)
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No (but either this + vectorBucketName or indexArn must be provided)
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The maximum number of vectors to return on a page.
   * If you don't specify maxResults, the ListVectors operation uses a default value of 500.
   * Valid Range: Minimum value of 1. Maximum value of 1000.
   * Required: No
   */
  @JsonProperty("maxResults")
  private Integer maxResults;

  /**
   * Pagination token from a previous request. The value of this field is empty for an initial request.
   * Length Constraints: Minimum length of 1. Maximum length of 2048.
   * Required: No
   */
  @JsonProperty("nextToken")
  private String nextToken;

  /**
   * If true, the vector data of each vector will be included in the response.
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnData")
  private Boolean returnData;

  /**
   * If true, the metadata associated with each vector will be included in the response.
   * The default value is false.
   * Required: No
   */
  @JsonProperty("returnMetadata")
  private Boolean returnMetadata;

  /**
   * For a parallel ListVectors request, segmentCount represents the total number of vector segments
   * into which the ListVectors operation will be divided.
   * Valid Range: Minimum value of 1. Maximum value of 16.
   * Required: No
   */
  @JsonProperty("segmentCount")
  private Integer segmentCount;

  /**
   * For a parallel ListVectors request, segmentIndex is the index of the segment from which to list
   * vectors in the current request.
   * Valid Range: Minimum value of 0. Maximum value of 15.
   * Required: No
   */
  @JsonProperty("segmentIndex")
  private Integer segmentIndex;

}
