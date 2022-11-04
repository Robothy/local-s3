package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import java.util.NavigableMap;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CreateMultipartUploadServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void createMultipartUpload(BucketService bucketService, ObjectService objectService) {
    String bucket = "my-bucket";
    bucketService.createBucket(bucket);
    String key = "a.txt";
    String uploadId = objectService.createMultipartUpload(bucket, key, CreateMultipartUploadOptions.builder()
        .contentType("plain/text")
        .build());
    LocalS3Metadata localS3Metadata = objectService.localS3Metadata();
    Optional<BucketMetadata> bucketMetadataOpt = localS3Metadata.getBucketMetadata(bucket);
    assertTrue(bucketMetadataOpt.isPresent());
    BucketMetadata bucketMetadata = bucketMetadataOpt.get();
    assertTrue(bucketMetadata.getUploads().containsKey(key));
    NavigableMap<String, UploadMetadata> uploadMetaMap = bucketMetadata.getUploads().get(key);
    assertTrue(uploadMetaMap.containsKey(uploadId));
    UploadMetadata uploadMetadata = uploadMetaMap.get(uploadId);
    assertEquals("plain/text", uploadMetadata.getContentType());
    assertTrue(System.currentTimeMillis() - uploadMetadata.getCreateDate() < 5000);
  }
}