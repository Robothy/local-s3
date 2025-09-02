package com.robothy.s3.core.model.internal.s3vectors;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Internal metadata representation for S3 Vector Objects.
 * This class stores vector metadata and references to vector data stored in VectorStorage.
 * The actual vector data is stored separately and referenced by storageId.
 */
@Data
@EqualsAndHashCode
public class VectorObjectMetadata {

  /**
   * Unique identifier for the vector object.
   */
  private String vectorId;

  /**
   * The number of dimensions in the vector data.
   */
  private int dimension;

  /**
   * User-defined metadata associated with the vector.
   * Key-value pairs for filtering and organization.
   */
  private JsonNode metadata;

  /**
   * Reference ID to the storage system for vector data.
   * Used to retrieve the actual vector data from VectorStorage.
   */
  private Long storageId;

  /**
   * Creation timestamp in milliseconds since epoch.
   */
  private long creationDate;

  /**
   * Create a VectorObjectMetadata instance.
   */
  public VectorObjectMetadata() {
  }

  /**
   * Create a VectorObjectMetadata instance with essential information.
   *
   * @param vectorId  the unique vector identifier
   * @param dimension the number of dimensions in the vector
   * @param storageId the storage ID referencing the vector data
   * @param metadata  user-defined metadata
   */
  public VectorObjectMetadata(String vectorId, int dimension, Long storageId, JsonNode metadata) {
    this.vectorId = vectorId;
    this.dimension = dimension;
    this.storageId = storageId;
    this.metadata = metadata;
    this.creationDate = System.currentTimeMillis();
  }

  /**
   * Validate that the vector dimensions match the expected dimension.
   *
   * @param expectedDimension the expected number of dimensions
   * @throws IllegalArgumentException if dimensions don't match
   */
  public void validateDimension(int expectedDimension) {
    if (this.dimension != expectedDimension) {
      throw new IllegalArgumentException(
          String.format("Vector dimension mismatch. Expected: %d, got: %d", expectedDimension, this.dimension)
      );
    }
  }

  /**
   * Check if this vector has the specified metadata key-value pair.
   *
   * @param key   the metadata key
   * @param value the metadata value
   * @return true if the metadata matches
   */
  public boolean hasMetadata(String key, String value) {
    return metadata != null && value.equals(metadata.get(key));
  }
}
