package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.VectorDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for creating a vector index.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_CreateIndex.html">CreateIndex API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIndexRequest {

  /**
   * The data type of the vectors to be inserted into the vector index.
   * Required: Yes
   */
  @JsonProperty("dataType")
  private VectorDataType dataType;

  /**
   * The dimensions of the vectors to be inserted into the vector index.
   * Valid Range: Minimum value of 1. Maximum value of 4096.
   * Required: Yes
   */
  @JsonProperty("dimension")
  private int dimension;

  /**
   * The distance metric to be used for similarity search.
   * Valid Values: euclidean | cosine
   * Required: Yes
   */
  @JsonProperty("distanceMetric")
  private DistanceMetric distanceMetric;

  /**
   * The name of the vector index to create.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: Yes
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * The metadata configuration for the vector index.
   * Required: No
   */
  @JsonProperty("metadataConfiguration")
  private MetadataConfiguration metadataConfiguration;

  /**
   * The Amazon Resource Name (ARN) of the vector bucket to create the vector index in.
   * Required: No
   */
  @JsonProperty("vectorBucketArn")
  private String vectorBucketArn;

  /**
   * The name of the vector bucket to create the vector index in.
   * Length Constraints: Minimum length of 3. Maximum length of 63.
   * Required: No
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

  /**
   * Metadata configuration for the vector index.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetadataConfiguration {

    /**
     * List of metadata keys that are not filterable.
     */
    @JsonProperty("nonFilterableMetadataKeys")
    private java.util.List<String> nonFilterableMetadataKeys;

  }
}
