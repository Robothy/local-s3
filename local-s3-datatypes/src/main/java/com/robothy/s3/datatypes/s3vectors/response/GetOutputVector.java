package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an output vector for GetVectors operation.
 * Contains vector key and optional data/metadata based on request parameters.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetOutputVector.html">GetOutputVector</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOutputVector {

  /**
   * The vector data (included if returnData was true in request).
   * Required: No
   */
  @JsonProperty("data")
  private PutInputVector.VectorData data;

  /**
   * The unique identifier for this vector.
   * Required: Yes
   */
  @JsonProperty("key")
  private String key;

  /**
   * User-defined metadata associated with this vector (included if returnMetadata was true in request).
   * Required: No
   */
  @JsonProperty("metadata")
  private JsonNode metadata;

}
