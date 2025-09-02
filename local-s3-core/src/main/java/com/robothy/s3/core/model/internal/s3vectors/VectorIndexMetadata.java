package com.robothy.s3.core.model.internal.s3vectors;

import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.VectorDataType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

/**
 * Internal metadata representation for S3 Vector Indexes.
 * This class stores the persistent configuration and state of a vector index.
 */
@Data
public class VectorIndexMetadata {

  /**
   * The name of the vector index.
   */
  private String indexName;

  /**
   * The number of dimensions for vectors in this index.
   * Must be between 1 and 4096 inclusive.
   */
  private int dimension;

  /**
   * The data type of vector elements.
   */
  private VectorDataType dataType;

  /**
   * The distance metric used for vector similarity calculations.
   */
  private DistanceMetric distanceMetric;

  /**
   * Schema for metadata fields that can be attached to vectors.
   * Key is the field name, value is the field type.
   */
  private Map<String, String> metadataSchema;

  /**
   * List of metadata keys that are not filterable.
   * These keys can be stored but cannot be used in queries for filtering.
   */
  private java.util.List<String> nonFilterableMetadataKeys;

  /**
   * Creation timestamp in milliseconds since epoch.
   */
  private long creationDate;

  /**
   * The current status of the vector index.
   * Possible values: "CREATING", "ACTIVE", "DELETING", "FAILED"
   */
  private String status;

  /**
   * Collection of vector objects stored in this index.
   * Key is the vector ID, value is the vector object metadata.
   */
  private Map<String, VectorObjectMetadata> vectorObjects;

  /**
   * Create a VectorIndexMetadata instance.
   */
  public VectorIndexMetadata() {
    this.status = "CREATING";
    this.vectorObjects = new ConcurrentHashMap<>();
  }

  /**
   * Set the dimension and validate it's within allowed range.
   * 
   * @param dimension the dimension to set
   * @throws IllegalArgumentException if dimension is out of range
   */
  public void setDimension(int dimension) {
    if (dimension < 1 || dimension > 4096) {
      throw new IllegalArgumentException("Vector dimension must be between 1 and 4096, got: " + dimension);
    }
    this.dimension = dimension;
  }

  /**
   * Check if the index is in ACTIVE status.
   * 
   * @return true if the index is active
   */
  public boolean isActive() {
    return "ACTIVE".equals(status);
  }

  /**
   * Mark the index as active.
   */
  public void setActive() {
    this.status = "ACTIVE";
  }

  /**
   * Mark the index as failed.
   */
  public void setFailed() {
    this.status = "FAILED";
  }

  /**
   * Mark the index as deleting.
   */
  public void setDeleting() {
    this.status = "DELETING";
  }

  /**
   * Add a vector object to this index.
   * 
   * @param vectorObject the vector object metadata to add
   * @return the previous vector object with the same ID, or null if none existed
   */
  public VectorObjectMetadata addVectorObject(VectorObjectMetadata vectorObject) {
    // Validate dimension compatibility
    vectorObject.validateDimension(this.dimension);
    return vectorObjects.put(vectorObject.getVectorId(), vectorObject);
  }

  /**
   * Get a vector object by ID.
   * 
   * @param vectorId the vector ID
   * @return the vector object metadata, or null if not found
   */
  public VectorObjectMetadata getVectorObject(String vectorId) {
    return vectorObjects.get(vectorId);
  }

  /**
   * Remove a vector object from this index.
   * 
   * @param vectorId the vector ID to remove
   * @return the removed vector object metadata, or null if not found
   */
  public VectorObjectMetadata removeVectorObject(String vectorId) {
    return vectorObjects.remove(vectorId);
  }

  /**
   * Check if a vector object exists in this index.
   * 
   * @param vectorId the vector ID to check
   * @return true if the vector exists
   */
  public boolean containsVectorObject(String vectorId) {
    return vectorObjects.containsKey(vectorId);
  }

  /**
   * Get the number of vector objects in this index.
   * 
   * @return the count of vector objects
   */
  public int getVectorObjectCount() {
    return vectorObjects.size();
  }

  /**
   * Clear all vector objects from this index.
   */
  public void clearVectorObjects() {
    vectorObjects.clear();
  }
}
