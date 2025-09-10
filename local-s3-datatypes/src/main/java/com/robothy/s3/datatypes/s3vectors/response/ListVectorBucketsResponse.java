package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for ListVectorBuckets operation.
 * Returns a list of all vector buckets owned by the authenticated sender.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListVectorBuckets.html">ListVectorBuckets API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVectorBucketsResponse {

  /**
   * The element is included in the response when there are more buckets to be listed
   * with pagination.
   * Type: String
   * Length Constraints: Minimum length of 1. Maximum length of 512.
   */
  @JsonProperty("nextToken")
  private String nextToken;

  /**
   * The list of vector buckets owned by the requester.
   * Type: Array of VectorBucketSummary objects
   */
  @JsonProperty("vectorBuckets")
  private List<VectorBucketSummary> vectorBuckets;

}
