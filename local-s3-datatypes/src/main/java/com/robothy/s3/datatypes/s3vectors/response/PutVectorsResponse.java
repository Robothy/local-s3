package com.robothy.s3.datatypes.s3vectors.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response object for PutVectors operation.
 * Contains lists of successful and failed vector keys.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_PutVectors.html">PutVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutVectorsResponse {
  
  /**
   * List of vector keys that failed to be put.
   * Only present if there were errors.
   */
  private List<String> errorVectorKeys;
}
