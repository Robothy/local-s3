package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for QueryVectors operation.
 * Contains the vectors found in the approximate nearest neighbor search.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_QueryVectors.html">QueryVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryVectorsResponse {

  /**
   * The vectors in the approximate nearest neighbor search.
   * Required: Yes
   */
  @JsonProperty("vectors")
  private List<QueryOutputVector> vectors;

}
