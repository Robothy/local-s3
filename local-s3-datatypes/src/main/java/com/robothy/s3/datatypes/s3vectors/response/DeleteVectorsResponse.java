package com.robothy.s3.datatypes.s3vectors.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response object for DeleteVectors operation.
 * Contains lists of successful and failed vector keys.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_DeleteVectors.html">DeleteVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteVectorsResponse {
  
  /**
   * List of vector keys that were successfully deleted.
   */
  private List<String> deletedVectorKeys;
  
  /**
   * List of vector keys that failed to be deleted.
   * Only present if there were errors.
   */
  private List<String> errorVectorKeys;
}
