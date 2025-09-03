package com.robothy.s3.core.model.internal.s3vectors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

/**
 * Represents vectors-related metadata of a local-s3 service.
 * This class manages vector buckets and their associated metadata.
 * Vector objects are managed through vector buckets, and actual vector data 
 * is stored separately using VectorStorage.
 */
@Getter
public class LocalS3VectorsMetadata {

  private static final Comparator<VectorBucketMetadata> DEFAULT_VECTOR_BUCKET_METADATA_COMPARATOR = Comparator
      .comparing(VectorBucketMetadata::getCreationDate);

  /**
   * -- GETTER --
   *  Get vector bucket metadata map of current
   *  instance.
   *  The key represents the vector bucket name, while the value represents the
   *  vector bucket metadata.
   *
   * @return a map of vector bucket metadata.
   */
  private final Map<String, VectorBucketMetadata> vectorBucketMetadataMap = new ConcurrentHashMap<>();

  /**
   * List ordered vector buckets with a comparator.
   *
   * @param comparator that determine the returned result order.
   * @return sorted vector bucket metadata.
   */
  public List<VectorBucketMetadata> listVectorBuckets(Comparator<VectorBucketMetadata> comparator) {
    ArrayList<VectorBucketMetadata> vectorBucketList = new ArrayList<>(vectorBucketMetadataMap.values());
    vectorBucketList.sort(comparator);
    return vectorBucketList;
  }

  /**
   * List vector buckets order by creation date.
   *
   * @return all vector buckets of current {@linkplain LocalS3VectorsMetadata}.
   */
  public List<VectorBucketMetadata> listVectorBuckets() {
    return listVectorBuckets(DEFAULT_VECTOR_BUCKET_METADATA_COMPARATOR);
  }

  /**
   * Get the metadata of {@code vectorBucketName}.
   *
   * @param vectorBucketName the vector bucket name.
   * @return vector bucket metadata.
   */
  public Optional<VectorBucketMetadata> getVectorBucketMetadata(String vectorBucketName) {
    return Optional.ofNullable(vectorBucketMetadataMap.get(vectorBucketName));
  }

  /**
   * Add a {@linkplain VectorBucketMetadata} instance.
   *
   * @param vectorBucketMetadata vector bucket metadata.
   * @throws IllegalArgumentException if the vector bucket name already exists.
   */
  public void addVectorBucketMetadata(VectorBucketMetadata vectorBucketMetadata) {
    if (vectorBucketMetadataMap.containsKey(vectorBucketMetadata.getVectorBucketName())) {
      throw new IllegalArgumentException("Vector bucket '" + vectorBucketMetadata.getVectorBucketName() + "' already exists");
    }
    this.vectorBucketMetadataMap.put(vectorBucketMetadata.getVectorBucketName(), vectorBucketMetadata);
  }

  /**
   * Remove a vector bucket and all its associated data.
   *
   * @param vectorBucketName the vector bucket name to remove.
   * @return the removed vector bucket metadata, or null if not found.
   */
  public VectorBucketMetadata removeVectorBucketMetadata(String vectorBucketName) {
    return vectorBucketMetadataMap.remove(vectorBucketName);
  }

  /**
   * Check if a vector bucket exists.
   *
   * @param vectorBucketName the vector bucket name.
   * @return true if the vector bucket exists, false otherwise.
   */
  public boolean vectorBucketExists(String vectorBucketName) {
    return vectorBucketMetadataMap.containsKey(vectorBucketName);
  }

}
