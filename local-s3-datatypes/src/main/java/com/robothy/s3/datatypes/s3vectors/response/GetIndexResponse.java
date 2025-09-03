package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robothy.s3.datatypes.s3vectors.VectorIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for retrieving a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetIndex.html">GetIndex API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetIndexResponse {

  /**
   * The attributes of the vector index.
   */
  @JsonProperty("index")
  private VectorIndex index;

}
