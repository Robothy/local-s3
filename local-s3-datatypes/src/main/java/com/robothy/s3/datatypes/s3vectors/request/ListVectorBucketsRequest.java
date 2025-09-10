package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for listing vector buckets.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListVectorBuckets.html">ListVectorBuckets API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVectorBucketsRequest {

  /**
   * The maximum number of vector buckets to be returned in the response.
   * Valid Range: Minimum value of 1. Maximum value of 500.
   * Required: No
   */
  @JsonProperty("maxResults")
  private Integer maxResults;

  /**
   * The previous pagination token.
   * Length Constraints: Minimum length of 1. Maximum length of 512.
   * Required: No
   */
  @JsonProperty("nextToken")
  private String nextToken;

  /**
   * Limits the response to vector buckets that begin with the specified prefix.
   * Length Constraints: Minimum length of 1. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("prefix")
  private String prefix;

}
