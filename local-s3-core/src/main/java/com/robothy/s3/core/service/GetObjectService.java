package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.asserionts.VersionedObjectAssertions;
import com.robothy.s3.core.exception.VersionedObjectNotExistException;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.storage.Storage;
import java.util.Optional;

public interface GetObjectService extends StorageApplicable, LocalS3MetadataApplicable {

  /**
   * Get object.
   */
  default GetObjectAns getObject(String bucketName, String key, GetObjectOptions options) {
    return getObject(localS3Metadata(), storage(), bucketName, key, false, options);
  }

  // Using static to make the target compatible with Java8
  static GetObjectAns getObject(LocalS3Metadata localS3Metadata, Storage storage,
                                String bucketName, String key, boolean metadataOnly, GetObjectOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata, bucketName);
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
  default GetObjectAns headObject(String bucketName, String key, GetObjectOptions options) {
    return getObject(localS3Metadata(), storage(), bucketName, key, true, options);
  }

}
