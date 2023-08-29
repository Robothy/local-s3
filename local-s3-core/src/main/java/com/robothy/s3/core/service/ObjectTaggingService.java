package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.exception.MethodNotAllowedException;
import com.robothy.s3.core.model.answers.GetObjectTaggingAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.util.VersionedObjectUtils;

/**
 * Object tagging service.
 */
public interface ObjectTaggingService extends LocalS3MetadataApplicable {

  /**
   * Put tagging to the specified versioned object.
   *
   * @param bucketName bucket name.
   * @param key object key.
   * @param versionId version ID.
   * @param tagging new tagging of the versioned object.
   * @return version ID where the new tagging applies to.
   */
  @BucketChanged
  @BucketWriteLock
  default String putObjectTagging(String bucketName, String key, String versionId, String[][] tagging) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    VersionedObjectMetadata versionedObjectMetadata = VersionedObjectUtils.getVersionedObjectMetadata(objectMetadata, versionId);
    if (versionedObjectMetadata.isDeleted()) {
      throw new MethodNotAllowedException("Cannot put object tagging to a delete marker.");
    }
    versionedObjectMetadata.setTagging(tagging);
    return VersionedObjectUtils.resolveReturnedVersion(objectMetadata, versionId);
  }

  /**
   * Get tagging from the specified versioned object.
   *
   * @param bucketName bucket name.
   * @param key object key.
   * @param versionId version ID.
   * @return versioned object tagging.
   */
  @BucketReadLock
  default GetObjectTaggingAns getObjectTagging(String bucketName, String key, String versionId) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    VersionedObjectMetadata versionedObjectMetadata = VersionedObjectUtils.getVersionedObjectMetadata(objectMetadata, versionId);
    if (versionedObjectMetadata.isDeleted()) {
      throw new MethodNotAllowedException("Cannot get object tagging from a delete marker.");
    }
    String[][] tagging = versionedObjectMetadata.getTagging().orElse(new String[0][0]);
    return GetObjectTaggingAns.builder()
        .tagging(tagging)
        .versionId(VersionedObjectUtils.resolveReturnedVersion(objectMetadata, versionId))
        .build();
  }

  /**
   * Delete object tagging from the specified versioned object.
   *
   * @param bucketName bucket name.
   * @param key object key.
   * @param versionId version ID.
   * @return version ID of the object where the tagging is deleted from.
   */
  @BucketChanged
  @BucketWriteLock
  default String deleteObjectTagging(String bucketName, String key, String versionId) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    VersionedObjectMetadata versionedObjectMetadata = VersionedObjectUtils.getVersionedObjectMetadata(objectMetadata, versionId);
    if (versionedObjectMetadata.isDeleted()) {
      throw new MethodNotAllowedException("Cannot delete object tagging from a delete marker.");
    }
    versionedObjectMetadata.setTagging(null);
    return VersionedObjectUtils.resolveReturnedVersion(objectMetadata, versionId);
  }

}
