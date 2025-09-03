package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for deleting vectors from a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_DeleteVectors.html">DeleteVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteVectorsRequest {

  /**
   * The ARN of the vector index that contains a vector you want to delete.
   * Required: No
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index that contains a vector you want to delete.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The keys of the vectors to delete.
   * Array Members: Minimum number of 1 item. Maximum number of 500 items.
   * Length Constraints: Minimum length of 1. Maximum length of 1024.
   * Required: Yes
   */
  @JsonProperty("keys")
  private List<String> keys;

  /**
   * The name of the vector bucket that contains the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

}
