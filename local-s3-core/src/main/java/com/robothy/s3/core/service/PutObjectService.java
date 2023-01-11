package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.util.IdUtils;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * <ul>
 *   <li>
 *     If bucket versioning is enabled, then create a new {@linkplain VersionedObjectMetadata} instance.
 *   </li>
 *   <li>
 *     If bucket versioning isn't enabled, then create a new {@linkplain VersionedObjectMetadata} instance,
 *     remove the virtual version object if exist, and set the version ID of the created
 *     {@linkplain VersionedObjectMetadata} as virtual version.
 *   </li>
 * </ul>
 *
 *
 */
public interface PutObjectService extends LocalS3MetadataApplicable, StorageApplicable {

  @BucketChanged
  default PutObjectAns putObject(String bucketName, String key, PutObjectOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);

    String versionId = IdUtils.defaultGenerator().nextStrId();
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    versionedObjectMetadata.setCreationDate(System.currentTimeMillis());
    versionedObjectMetadata.setContentType(options.getContentType());
    versionedObjectMetadata.setSize(options.getSize());
    Long fileId = storage().put(options.getContent());
    versionedObjectMetadata.setFileId(fileId);
    options.getTagging().ifPresent(versionedObjectMetadata::setTagging);

    ObjectMetadata objectMetadata;
    if (bucketMetadata.getObjectMetadata(key).isPresent()) {
      objectMetadata = bucketMetadata.getObjectMetadata(key).get();
      objectMetadata.putVersionedObjectMetadata(versionId, versionedObjectMetadata);
    } else {
      objectMetadata = new ObjectMetadata(versionId, versionedObjectMetadata);
      bucketMetadata.putObjectMetadata(key, objectMetadata);
    }

    String returnedVersionId = versionId;
    if (!Boolean.TRUE.equals(bucketMetadata.getVersioningEnabled())) {
      returnedVersionId = Objects.isNull(bucketMetadata.getVersioningEnabled()) ? null : ObjectMetadata.NULL_VERSION;

      Optional<String> virtualVersionOpt = objectMetadata.getVirtualVersion();
      if (virtualVersionOpt.isPresent()) {
        String lastVirtualVersion = virtualVersionOpt.get();
        VersionedObjectMetadata previousVersion = objectMetadata.getVersionedObjectMap().remove(lastVirtualVersion);
        if (Objects.nonNull(previousVersion.getFileId())) { // Not a delete marker.
          storage().delete(previousVersion.getFileId());
        }

        objectMetadata.setVirtualVersion(versionId);
      } else {
        objectMetadata.setVirtualVersion(versionId);
      }
    }

    return PutObjectAns.builder()
        .key(key)
        .versionId(returnedVersionId)
        .creationDate(versionedObjectMetadata.getCreationDate())
        .build();
  }

}
