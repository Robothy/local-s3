package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.LocalS3BadDigestException;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.core.util.S3ObjectUtils;

import java.util.Base64;
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
  @BucketWriteLock
  default PutObjectAns putObject(String bucketName, String key, PutObjectOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);

    String versionId = IdUtils.defaultGenerator().nextStrId();
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    versionedObjectMetadata.setCreationDate(System.currentTimeMillis());
    versionedObjectMetadata.setContentType(options.getContentType());
    versionedObjectMetadata.setSize(options.getSize());
    if (Objects.nonNull(options.getUserMetadata())) {
      versionedObjectMetadata.setUserMetadata(options.getUserMetadata());
    }
    Long fileId = storage().put(options.getContent());
    versionedObjectMetadata.setFileId(fileId);

    versionedObjectMetadata.setEtag(S3ObjectUtils.etag(storage().getInputStream(fileId)));
    checkRequestingMd5Header(options, fileId, versionedObjectMetadata.getEtag());
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
        .etag(versionedObjectMetadata.getEtag())
        .build();
  }

  private void checkRequestingMd5Header(PutObjectOptions options, Long fileId, String etag) {
    // Validate Content-MD5 header if present.
    if (Objects.nonNull(options.getContentMd5())) {
      try {
        byte[] md5Bytes = org.apache.commons.codec.binary.Hex.decodeHex(etag);
        String computedBase64 = Base64.getEncoder().encodeToString(md5Bytes);
        if (!computedBase64.equals(options.getContentMd5())) {
          storage().delete(fileId);
          throw new LocalS3BadDigestException("The Content-MD5 you specified did not match what we received.");
        }
      } catch (org.apache.commons.codec.DecoderException e) {
        storage().delete(fileId);
        throw new LocalS3BadDigestException("Invalid Content-MD5 header.");
      }
    }
  }

}
