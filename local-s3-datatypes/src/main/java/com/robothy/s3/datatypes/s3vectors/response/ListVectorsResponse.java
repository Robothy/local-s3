package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robothy.s3.datatypes.s3vectors.ListOutputVector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response object for listing vectors in a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListVectors.html">ListVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVectorsResponse {

  /**
   * Vectors in the current segment.
   */
  @JsonProperty("vectors")
  private List<ListOutputVector> vectors;

  /**
   * Pagination token to be used in the subsequent request.
   * The field is empty if no further pagination is required.
   * Length Constraints: Minimum length of 1. Maximum length of 2048.
   */
  @JsonProperty("nextToken")
  private String nextToken;

}
