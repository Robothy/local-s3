package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.asserionts.VersionedObjectAssertions;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.VersionedObjectNotExistException;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.storage.Storage;
import java.util.Objects;
import java.util.Optional;

public interface GetObjectService extends StorageApplicable, LocalS3MetadataApplicable {

  /**
   * Get object.
   */
  @BucketReadLock
  default GetObjectAns getObject(String bucketName, String key, GetObjectOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    if (Objects.isNull(bucketMetadata.getVersioningEnabled())) {
      return getObjectFromUnVersionedBucket(bucketMetadata, storage(), bucketName, key, false, options);
    }
    return getObject(bucketMetadata, storage(), bucketName, key, false, options);
  }

  static GetObjectAns getObjectFromUnVersionedBucket(BucketMetadata bucketMetadata, Storage storage,
                                                     String bucketName, String key, boolean metadataOnly, GetObjectOptions options) {
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    if (options.getVersionId().isPresent() && !ObjectMetadata.NULL_VERSION.equals(options.getVersionId().get())) {
      throw new LocalS3InvalidArgumentException("versionId", options.getVersionId().get());
    }

    VersionedObjectMetadata latestObject = objectMetadata.getLatest();

    return GetObjectAns.builder()
        .bucketName(bucketName)
        .key(key)
        .contentType(latestObject.getContentType())
        .lastModified(latestObject.getCreationDate())
        .size(latestObject.getSize())
        .content(metadataOnly ? null : storage.getInputStream(latestObject.getFileId()))
        .etag(latestObject.getEtag())
        .userMetadata(latestObject.getUserMetadata())
        .taggingCount(latestObject.getTagging().map(tagging -> tagging.length).orElse(0))
        .build();
  }

  // Using static to make the target compatible with Java8
  static GetObjectAns getObject(BucketMetadata bucketMetadata, Storage storage,
                                String bucketName, String key, boolean metadataOnly, GetObjectOptions options) {

    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    Optional<String> versionIdOpt = options.getVersionId();
    VersionedObjectMetadata versionedObjectMetadata;

    String returnedVersionId;
    if (versionIdOpt.isPresent()) {
      if (ObjectMetadata.NULL_VERSION.equals(versionIdOpt.get())) {
        versionedObjectMetadata = VersionedObjectAssertions.assertVirtualVersionExist(objectMetadata);
      } else {
        // Cannot access an object with virtual version.
        if (objectMetadata.getVirtualVersion().map(versionIdOpt.get()::equals).orElse(false)) {
          throw new VersionedObjectNotExistException(key, versionIdOpt.get());
        }
        versionedObjectMetadata = VersionedObjectAssertions
            .assertVersionedObjectExist(objectMetadata, versionIdOpt.get());
      }
      returnedVersionId = versionIdOpt.get();
    } else {
      versionedObjectMetadata = objectMetadata.getLatest();

      // If the latest version is virtual version, then map the virtual version to "null".
      if (objectMetadata.getVirtualVersion()
          .map(virtualVersion -> objectMetadata.getLatestVersion().equals(virtualVersion)).orElse(false)) {
        returnedVersionId = ObjectMetadata.NULL_VERSION;
      } else {
        returnedVersionId = objectMetadata.getLatestVersion();
      }
    }

    if (versionedObjectMetadata.isDeleted()) {

      // The version ID is not specified and the latest version is a delete-marker.
      if (!versionIdOpt.isPresent()) {
        throw new ObjectNotExistException(key);
      }

      return GetObjectAns.builder()
          .bucketName(bucketName)
          .key(key)
          .deleteMarker(true)
          .versionId(returnedVersionId)
          .lastModified(versionedObjectMetadata.getCreationDate())
          .build();
    } else {
      return GetObjectAns.builder()
          .bucketName(bucketName)
          .key(key)
          .versionId(returnedVersionId)
          .contentType(versionedObjectMetadata.getContentType())
          .lastModified(versionedObjectMetadata.getCreationDate())
          .size(versionedObjectMetadata.getSize())
          .content(metadataOnly ? null : storage.getInputStream(versionedObjectMetadata.getFileId()))
          .etag(versionedObjectMetadata.getEtag())
          .taggingCount(versionedObjectMetadata.getTagging().map(tagging -> tagging.length).orElse(0))
          .userMetadata(versionedObjectMetadata.getUserMetadata())
          .build();
    }
  }

  /**
   * Get metadata of the specified object.
   *
   * @param bucketName the bucket name.
   * @param key the object key.
   * @param options options.
   * @return versioned object with metadata only.
   */
  @BucketReadLock
  default GetObjectAns headObject(String bucketName, String key, GetObjectOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    if (Objects.isNull(bucketMetadata.getVersioningEnabled())) {
      return getObjectFromUnVersionedBucket(bucketMetadata, storage(), bucketName, key, true, options);
    }

    return getObject(bucketMetadata, storage(), bucketName, key, true, options);
  }

}
