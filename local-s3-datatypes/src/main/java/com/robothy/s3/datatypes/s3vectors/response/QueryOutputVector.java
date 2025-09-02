package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an output vector for QueryVectors operation.
 * Contains vector key and optional distance, data, and metadata based on request parameters.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_QueryOutputVector.html">QueryOutputVector</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryOutputVector {

  /**
   * The vector data (included if returnData was true in request).
   * Required: No
   */
  @JsonProperty("data")
  private PutInputVector.VectorData data;

  /**
   * The computed distance between the query vector and this vector.
   * Included if returnDistance was true in request.
   * Required: No
   */
  @JsonProperty("distance")
  private Double distance;

  /**
   * The unique identifier for this vector.
   * Required: Yes
   */
  @JsonProperty("key")
  private String key;

  /**
   * User-defined metadata associated with this vector.
   * Included if returnMetadata was true in request.
   * Required: No
   */
  @JsonProperty("metadata")
  private JsonNode metadata;

}
