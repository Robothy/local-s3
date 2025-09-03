package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an input vector for PutVectors operation.
 * Contains the vector data and optional metadata.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_PutInputVector.html">PutInputVector</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutInputVector {

  /**
   * The vector data as float32 array.
   * Required: Yes
   */
  @JsonProperty("data")
  private VectorData data;

  /**
   * The unique identifier for this vector.
   * Length Constraints: Minimum length of 1. Maximum length of 1024.
   * Required: Yes
   */
  @JsonProperty("key")
  private String key;

  /**
   * User-defined metadata associated with this vector.
   * Used for filtering during vector search operations.
   * Required: No
   */
  @JsonProperty("metadata")
  private JsonNode metadata;

  /**
   * Represents the vector data structure.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VectorData {

    /**
     * The vector data as an array of float32 values.
     * Required: Yes
     */
    @JsonProperty("float32")
    private float[] values;

  }
}
