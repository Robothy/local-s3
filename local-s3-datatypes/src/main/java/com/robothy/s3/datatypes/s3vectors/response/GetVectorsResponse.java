package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for GetVectors operation.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetVectors.html">GetVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetVectorsResponse {

  /**
   * The attributes of the vectors.
   * Type: Array of GetOutputVector objects
   */
  @JsonProperty("vectors")
  private List<GetOutputVector> vectors;

  /**
   * List of vector keys that failed to be retrieved.
   * Only present if there were errors.
   */
  @JsonProperty("errorVectorKeys")
  private List<String> errorVectorKeys;

}
