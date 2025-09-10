package com.robothy.s3.datatypes.s3vectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The attributes of a vector returned by the ListVectors operation.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListOutputVector.html">ListOutputVector API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListOutputVector {

  /**
   * The name of the vector.
   * Length Constraints: Minimum length of 1. Maximum length of 1024.
   * Required: Yes
   */
  @JsonProperty("key")
  private String key;

  /**
   * The vector data of the vector.
   * Only included if returnData=true in the request.
   * Required: No
   */
  @JsonProperty("data")
  private PutInputVector.VectorData data;

  /**
   * Metadata about the vector.
   * Only included if returnMetadata=true in the request.
   * Required: No
   */
  @JsonProperty("metadata")
  private JsonNode metadata;

}
