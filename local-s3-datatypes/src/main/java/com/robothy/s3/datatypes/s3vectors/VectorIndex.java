package com.robothy.s3.datatypes.s3vectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robothy.s3.datatypes.s3vectors.request.CreateIndexRequest;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents an S3 Vector Index following AWS S3 Vectors API specification.
 * A vector index defines the structure and search configuration for vectors in a vector bucket.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_Index.html">Index object</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VectorIndex {

  /**
   * The creation date and time of the vector index as Unix timestamp.
   */
  @JsonProperty("creationTime")
  private Long creationTime;

  /**
   * The data type of vector elements.
   */
  @JsonProperty("dataType")
  private VectorDataType dataType;

  /**
   * The number of dimensions for vectors in this index.
   * Must be between 1 and 4096 inclusive.
   */
  @JsonProperty("dimension")
  private int dimension;

  /**
   * The distance metric used for vector similarity calculations.
   */
  @JsonProperty("distanceMetric")
  private DistanceMetric distanceMetric;

  /**
   * The Amazon Resource Name (ARN) of the vector index.
   * Format: arn:aws:s3vectors:::vector-bucket/{bucketName}/index/{indexName}
   */
  @JsonProperty("indexArn")
  private String indexArn;

  /**
   * The name of the vector index within the vector bucket.
   */
  @JsonProperty("indexName")
  private String indexName;

  /**
   * Configuration for metadata that can be attached to vectors.
   */
  @JsonProperty("metadataConfiguration")
  private CreateIndexRequest.MetadataConfiguration metadataConfiguration;

  /**
   * The name of the vector bucket that contains this index.
   */
  @JsonProperty("vectorBucketName")
  private String vectorBucketName;

  /**
   * The current status of the vector index.
   * Possible values: "CREATING", "ACTIVE", "DELETING", "FAILED"
   */
  @JsonProperty("status")
  private String status;

  /**
   * The number of vectors currently stored in this index.
   */
  @JsonProperty("vectorCount")
  private Long vectorCount;

  /**
   * Validate the dimension is within allowed range.
   * 
   * @param dimension the dimension to validate
   * @throws IllegalArgumentException if dimension is out of range
   */
  public static void validateDimension(int dimension) {
    if (dimension < 1 || dimension > 4096) {
      throw new IllegalArgumentException("Vector dimension must be between 1 and 4096, got: " + dimension);
    }
  }

  /**
   * Generate the ARN for a vector index.
   * 
   * @param bucketName the name of the vector bucket
   * @param indexName the name of the vector index
   * @return the ARN string
   */
  public static String generateArn(String bucketName, String indexName) {
    return String.format("arn:aws:s3vectors:::vector-bucket/%s/index/%s", bucketName, indexName);
  }

  /**
   * Set the dimension and validate it's within allowed range.
   * 
   * @param dimension the dimension to set
   */
  public void setDimension(int dimension) {
    validateDimension(dimension);
    this.dimension = dimension;
  }

  /**
   * Set creation time from Instant.
   * 
   * @param instant the creation time as Instant
   */
  public void setCreationTimeFromInstant(Instant instant) {
    this.creationTime = instant != null ? instant.getEpochSecond() : null;
  }

  /**
   * Get creation time as Instant.
   * 
   * @return the creation time as Instant, or null if not set
   */
  public Instant getCreationTimeAsInstant() {
    return creationTime != null ? Instant.ofEpochSecond(creationTime) : null;
  }
}
