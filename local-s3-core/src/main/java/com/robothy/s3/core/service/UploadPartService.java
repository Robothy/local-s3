package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.UploadAssertions;
import com.robothy.s3.core.model.answers.UploadPartAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import com.robothy.s3.core.model.request.UploadPartOptions;
import com.robothy.s3.core.util.S3ObjectUtils;
import java.util.NavigableMap;

/**
 * Uploads a part in a multipart upload.
 */
public interface UploadPartService extends LocalS3MetadataApplicable, StorageApplicable {

  /**
   * Upload part for an initialized upload.
   *
   * @param bucket the bucket name.
   * @param key the object key of the upload.
   * @param uploadId the upload ID generated when initializing the upload.
   * @param partNumber the part number.
   * @param options options of upload the upload part operation.
   * @return result of the upload part.
   */
  @BucketChanged
  @BucketWriteLock
  default UploadPartAns uploadPart(String bucket, String key, String uploadId, Integer partNumber, UploadPartOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);
    UploadMetadata uploadMetadata = UploadAssertions.assertUploadExists(bucketMetadata, key, uploadId);
    NavigableMap<Integer, UploadPartMetadata> parts = uploadMetadata.getParts();
    if (parts.containsKey(partNumber)) {
      UploadPartMetadata uploadPartMetadata = parts.get(partNumber);
      storage().delete(uploadPartMetadata.getFileId());
    }

    Long fileId = storage().put(options.getData());
    UploadPartMetadata uploadPartMetadata = UploadPartMetadata.builder()
        .fileId(fileId)
        .lastModified(System.currentTimeMillis())
        .size(options.getContentLength())
        .etag(options.getETag().orElseGet(() -> S3ObjectUtils.etag(storage().getInputStream(fileId))))
        .build();
    parts.put(partNumber, uploadPartMetadata);
    return UploadPartAns.builder()
        .etag(uploadPartMetadata.getEtag())
        .build();
  }

}
