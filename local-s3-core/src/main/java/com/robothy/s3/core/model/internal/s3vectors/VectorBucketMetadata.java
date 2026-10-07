package com.robothy.s3.core.model.internal.s3vectors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.robothy.s3.datatypes.s3vectors.EncryptionConfiguration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

/**
 * Internal metadata representation for S3 Vector Buckets.
 * This class follows the same patterns as {@link com.robothy.s3.core.model.internal.BucketMetadata}
 * for storage and persistence in LocalS3.
 */
@Data
public class VectorBucketMetadata {

  /**
   * Create a {@linkplain VectorBucketMetadata} instance.
   */
  public VectorBucketMetadata() {
  }

  /**
   * The name of the vector bucket.
   */
  private String vectorBucketName;

  /**
   * Creation timestamp in milliseconds since epoch.
   */
  private long creationDate;

  /**
   * Optional encryption configuration for the vector bucket.
   */
  private EncryptionConfiguration encryptionConfiguration;

  /**
   * The bucket policy JSON for this vector bucket.
   * This field stores the complete policy document as JSON text.
   */
  private String policy;

  /**
   * Map of vector indexes in this bucket.
   * Key: index name, Value: index metadata
   */
  @JsonDeserialize(converter = VectorIndexMetadataMapConverter.class)
  private ConcurrentSkipListMap<String, VectorIndexMetadata> indexes = new ConcurrentSkipListMap<>();

  /**
   * Map of vector objects in this bucket.
   * Key: vector ID, Value: vector object metadata
   */
  @JsonDeserialize(converter = VectorObjectMetadataMapConverter.class)
  private ConcurrentSkipListMap<String, VectorObjectMetadata> vectorData = new ConcurrentSkipListMap<>();

  /**
   * Tags associated with this vector bucket.
   * Key: tag key, Value: tag value
   */
  private Map<String, String> tags = new ConcurrentHashMap<>();

  /**
   * Get encryption configuration for this vector bucket.
   *
   * @return Optional encryption configuration
   */
  public Optional<EncryptionConfiguration> getEncryptionConfiguration() {
    return Optional.ofNullable(encryptionConfiguration);
  }


  /**
   * Get metadata of the specified vector index.
   *
   * @param indexName the index name
   * @return Optional index metadata
   */
  public Optional<VectorIndexMetadata> getIndexMetadata(String indexName) {
    return Optional.ofNullable(indexes.get(indexName));
  }

  /**
   * Add a vector index to this bucket.
   *
   * @param indexName     the index name
   * @param indexMetadata the index metadata
   * @return the added index metadata
   */
  public VectorIndexMetadata putIndexMetadata(String indexName, VectorIndexMetadata indexMetadata) {
    if (indexName == null || indexName.trim().isEmpty()) {
      throw new IllegalArgumentException("Index name cannot be null or empty");
    }
    indexes.put(indexName, indexMetadata);
    return indexMetadata;
  }

  /**
   * Remove a vector index from this bucket.
   *
   * @param indexName the index name
   * @return the removed index metadata, or null if not found
   */
  public VectorIndexMetadata removeIndexMetadata(String indexName) {
    return indexes.remove(indexName);
  }

  /**
   * Get the bucket policy JSON for this vector bucket.
   *
   * @return Optional policy JSON string
   */
  public Optional<String> getPolicy() {
    return Optional.ofNullable(policy);
  }

  /**
   * Set policy JSON for current vector bucket.
   *
   * @param policy policy JSON string, or null to remove policy
   */
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * Get the tags of this vector bucket.
   * Always returns a non-null map, even if deserialized metadata lacks tags.
   *
   * @return non-null map of tag key to tag value
   */
  public Map<String, String> getTags() {
    if (tags == null) {
      tags = new ConcurrentHashMap<>();
    }
    return tags;
  }

  /**
   * Add or update a tag on this vector bucket.
   *
   * @param key   the tag key
   * @param value the tag value
   * @return the previous value associated with the key, or null
   */
  public String putTag(String key, String value) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Tag key cannot be null or empty");
    }
    return getTags().put(key, value);
  }

  /**
   * Remove a tag from this vector bucket.
   *
   * @param key the tag key
   * @return the removed value, or null if the key did not exist
   */
  public String removeTag(String key) {
    return getTags().remove(key);
  }
}
