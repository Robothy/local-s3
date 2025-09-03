package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for ListIndexes operation.
 * Returns a list of all vector indexes within the specified vector bucket.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListIndexes.html">ListIndexes API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListIndexesResponse {

  /**
   * The attributes of the vector indexes.
   * Type: Array of IndexSummary objects
   */
  @JsonProperty("indexes")
  private List<IndexSummary> indexes;

  /**
   * The next pagination token.
   * Type: String
   * Length Constraints: Minimum length of 1. Maximum length of 512.
   */
  @JsonProperty("nextToken")
  private String nextToken;

}
