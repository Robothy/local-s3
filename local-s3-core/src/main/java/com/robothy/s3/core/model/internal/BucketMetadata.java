package com.robothy.s3.core.model.internal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.converters.deserializer.ObjectMetadataMapConverter;
import com.robothy.s3.core.converters.deserializer.UploadMetadataMapConverter;
import com.robothy.s3.datatypes.AccessControlPolicy;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

@Data
public class BucketMetadata {

  public BucketMetadata() {

  }

  @JsonDeserialize(converter = ObjectMetadataMapConverter.class)
  private ConcurrentSkipListMap<String, ObjectMetadata> objectMap = new ConcurrentSkipListMap<>();

  private long creationDate;

  /**
   * key - object key.
   */
  @JsonDeserialize(converter = UploadMetadataMapConverter.class)
  private NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = new ConcurrentSkipListMap<>();

  /**
   * null - default, user not set versioning.
   * true - enabled
   * false - suspended
   */
  private Boolean versioningEnabled;

  private String bucketName;

  private Collection<Map<String, String>> tagging;

  private AccessControlPolicy acl;

  private String policy;

  private String replication;

  public Optional<ObjectMetadata> getObjectMetadata(String key) {
    return Optional.ofNullable(objectMap.get(key));
  }


  /**
   * Add an {@linkplain ObjectMetadata} instance.
   *
   * @param objectMetadata object metadata.
   * @return added object metadata.
   */
  public ObjectMetadata addObjectMetadata(ObjectMetadata objectMetadata) {
    ObjectAssertions.assertObjectKeyIsValid(objectMetadata.getKey());
    objectMap.put(objectMetadata.getKey(), objectMetadata);
    return objectMetadata;
  }

  /**
   * Get tagging of current bucket.
   *
   * @return tagging of current bucket.
   */
  public Optional<Collection<Map<String, String>>> getTagging() {
    return Optional.ofNullable(tagging);
  }

  /**
   * Set tagging of current bucket. The tagging will be completely overridden if exists.
   *
   * @param tagging new tagging.
   */
  public void setTagging(Collection<Map<String, String>> tagging) {
    this.tagging = tagging;
  }

  /**
   * Get ACL of current bucket.
   *
   * @return the ACL of current bucket.
   */
  public Optional<AccessControlPolicy> getAcl() {
    return Optional.ofNullable(acl);
  }

  /**
   * Set ACL of this bucket.
   *
   * @param acl new ACL.
   */
  public void setAcl(AccessControlPolicy acl) {
    this.acl = acl;
  }

  public Optional<String> getPolicy() {
    return Optional.ofNullable(policy);
  }

  /**
   * Set policy JSON for current bucket.
   *
   * @param policy policy.
   */
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * Get replication configuration.
   *
   * @return replication configuration of current bucket.
   */
  public Optional<String> getReplication() {
    return Optional.ofNullable(replication);
  }

  /**
   * Set replication configuration.
   *
   * @param replication replication configuration.
   */
  public void setReplication(String replication) {
    this.replication = replication;
  }

}
