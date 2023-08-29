package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.util.IdUtils;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public interface CreateMultipartUploadService extends LocalS3MetadataApplicable {

  /**
   * Init a multipart upload.
   *
   * @param bucket  the bucket name
   * @param key     the object key.
   * @param options options of the multipart upload.
   * @return the upload ID.
   */
  @BucketChanged
  @BucketWriteLock
  default String createMultipartUpload(String bucket, String key, CreateMultipartUploadOptions options) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);
    ObjectAssertions.assertObjectKeyIsValid(key);
    String uploadId = IdUtils.defaultGenerator().nextStrId();
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = bucketMetadata.getUploads();
    uploads.putIfAbsent(key, new ConcurrentSkipListMap<>());
    uploads.get(key).put(uploadId, UploadMetadata.builder()
        .contentType(options.getContentType())
        .createDate(System.currentTimeMillis())
        .build());
    return uploadId;
  }


}
