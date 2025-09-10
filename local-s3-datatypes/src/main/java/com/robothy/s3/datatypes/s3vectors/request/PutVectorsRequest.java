package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for putting vectors into a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_PutVectors.html">PutVectors API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutVectorsRequest {

  /**
   * The ARN of the vector index where you want to write vectors.
   * Required: No
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index where you want to write vectors.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The name of the vector bucket that contains the vector index.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

  /**
   * The vectors to add to a vector index.
   * Array Members: Minimum number of 1 item. Maximum number of 500 items.
   * Required: Yes
   */
  @JsonProperty("vectors")
  private List<PutInputVector> vectors;

}
