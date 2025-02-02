package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import java.util.NavigableMap;

/**
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_AbortMultipartUpload.html">AbortMultipartUpload</a>
 */
public interface AbortMultipartUploadService extends LocalS3MetadataApplicable, StorageApplicable {

  /**
   * If the upload ID is not found, do nothing. Otherwise, abort the multipart upload
   * and release the storage space.
   *
   * @param bucketName bucket name.
   * @param objectKey object key.
   * @param uploadId upload ID.
   */
  @BucketChanged
  @BucketWriteLock
  default void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
    LocalS3Metadata s3Metadata = localS3Metadata();
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    ObjectAssertions.assertObjectKeyIsValid(objectKey);
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = bucketMetadata.getUploads();
    if (!uploads.containsKey(objectKey) || !uploads.get(objectKey).containsKey(uploadId)) {
      // do nothing if the uploadId is not found.
      return;
    }

    UploadMetadata uploadMetadata = uploads.get(objectKey).remove(uploadId);
    uploadMetadata.getParts().forEach((uploadNumber, part) -> {
      storage().delete(part.getFileId());
    });

    if (uploads.get(objectKey).isEmpty()) {
      uploads.remove(objectKey);
    }

    // help GC.
    uploadMetadata.getParts().clear();
  }

}
