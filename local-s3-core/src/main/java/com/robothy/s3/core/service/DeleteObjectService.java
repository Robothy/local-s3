package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.util.IdUtils;
import java.util.Objects;
import java.util.Optional;

/**
 * Delete object operation.
 */
public interface DeleteObjectService extends LocalS3MetadataApplicable, StorageApplicable {

  @BucketChanged
  default DeleteObjectAns deleteObject(String bucketName, String key) {
    return deleteObject(bucketName, key, null);
  }

  @BucketChanged
  default DeleteObjectAns deleteObject(String bucketName, String key, String versionId) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    return Objects.isNull(versionId) ? deleteWithoutVersionId(bucketMetadata, key)
        : deleteWithVersionId(bucketMetadata, key, versionId);
  }

  /**
   * Delete without version ID.
   * <ul>
   *   <li>If the key exists</li>
   *   <ul>
   *     <li>If bucket versioning enabled, create a delete marker with version ID.</li>
   *     <li>If bucket versioning disabled, create a delete marker with virtual version.</li>
   *   </ul>
   *   <li>If the key not exists.</li>
   *   <ul>
   *     <li>If bucket versioning enabled, create the object with a deleted marker with version ID.</li>
   *     <li>If bucket versioning suspended, create the object with a deleted marker with virtual version ID.</li>
   *   </ul>
   * </ul>
   */
  private DeleteObjectAns deleteWithoutVersionId(BucketMetadata bucketMetadata, String key) {
    Optional<ObjectMetadata> objectMetadataOpt = bucketMetadata.getObjectMetadata(key);
    String returnedVersionId;
    if (objectMetadataOpt.isPresent()) { // key exists
      ObjectMetadata objectMetadata = objectMetadataOpt.get();
      VersionedObjectMetadata deleteMarker = createDeleteMarker();
      String versionId = IdUtils.defaultGenerator().nextStrId();
      if (Boolean.TRUE.equals(bucketMetadata.getVersioningEnabled())) { // versioning enabled
        objectMetadata.putVersionedObjectMetadata(versionId, deleteMarker);
        returnedVersionId = versionId;
      } else { // versioning disabled.
        objectMetadata.putVersionedObjectMetadata(versionId, deleteMarker);
        if (objectMetadata.getVirtualVersion().isPresent()) {
          VersionedObjectMetadata removed =
              objectMetadata.getVersionedObjectMap().remove(objectMetadata.getVirtualVersion().get());
          if (!removed.isDeleted()) {
            storage().delete(removed.getFileId());
          }
        }
        objectMetadata.setVirtualVersion(versionId);
        returnedVersionId = ObjectMetadata.NULL_VERSION;
      }
    } else { // key not exists.
      VersionedObjectMetadata deleteMarker = createDeleteMarker();
      String versionId = returnedVersionId = IdUtils.defaultGenerator().nextStrId();
      ObjectMetadata objectMetadata = new ObjectMetadata(key, versionId, deleteMarker);
      if (!Boolean.TRUE.equals(bucketMetadata.getVersioningEnabled())) {
        objectMetadata.setVirtualVersion(versionId);
        returnedVersionId = ObjectMetadata.NULL_VERSION;
      }
      bucketMetadata.addObjectMetadata(objectMetadata);
    }

    return DeleteObjectAns.builder()
        .isDeleteMarker(true)
        .versionId(returnedVersionId)
        .build();
  }

  private VersionedObjectMetadata createDeleteMarker() {
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    versionedObjectMetadata.setDeleted(true);
    versionedObjectMetadata.setCreationDate(System.currentTimeMillis());
    return versionedObjectMetadata;
  }

  /**
   * Delete with version ID.
   * <ul>
   *    <li>The object key must exist(This behavior is not the same as AmazonS3).</li>
   *    <li>If the version ID is 'null', try to find the virtual version object and remove.</li>
   *    <li>If the version ID is exists, find the versioned object and remove.</li>
   *    <li>If the version ID is not exists, do nothing and return the given version ID.</li>
   * </ul>
   */
  private DeleteObjectAns deleteWithVersionId(BucketMetadata bucketMetadata, String key, String versionId) {
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    boolean isDeleteMarker = false;
    if (ObjectMetadata.NULL_VERSION.equals(versionId)) {
      Optional<String> virtualVersionOpt = objectMetadata.getVirtualVersion();
      if (virtualVersionOpt.isPresent()) {
        VersionedObjectMetadata toRemove = objectMetadata.getVersionedObjectMap().remove(virtualVersionOpt.get());
        isDeleteMarker = toRemove.isDeleted();
        if (!isDeleteMarker) {
          storage().delete(toRemove.getFileId());
        }
        objectMetadata.setVirtualVersion(null);
      }
    } else {
      Optional<VersionedObjectMetadata> versionedObjectMetadataOpt = objectMetadata.getVersionedObjectMetadata(versionId);
      if (versionedObjectMetadataOpt.isPresent()
          && !objectMetadata.getVirtualVersion().map(versionId::equals).orElse(false)) {
        VersionedObjectMetadata removed = objectMetadata.getVersionedObjectMap().remove(versionId);
        isDeleteMarker = removed.isDeleted();
        if (!isDeleteMarker) {
          storage().delete(removed.getFileId());
        }
      }
    }

    if (objectMetadata.getVersionedObjectMap().isEmpty()) {
      bucketMetadata.getObjectMap().remove(key);
    }

    return DeleteObjectAns.builder()
        .isDeleteMarker(isDeleteMarker)
        .versionId(versionId)
        .build();
  }

}
