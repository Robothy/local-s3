package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for DeleteVectorBucket operation.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_DeleteVectorBucket.html">DeleteVectorBucket API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteVectorBucketRequest {

  /**
   * The name of the vector bucket to delete.
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
