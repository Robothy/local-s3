package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.asserionts.UploadAssertions;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.model.request.UploadPartOptions;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UploadPartServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void uploadPart(BucketService bucketService, ObjectService objectService) {
    String bucket = "my-bucket";
    String key = "a.txt";
    bucketService.createBucket(bucket);

    assertThrows(ObjectNotExistException.class, () -> objectService.uploadPart(bucket, key, "123", 1, null));
    String uploadId = objectService.createMultipartUpload(bucket, key,
        CreateMultipartUploadOptions.builder().contentType("plain/text").build());
    assertThrows(UploadNotExistException.class, () -> objectService.uploadPart(bucket, key, "123", 1, null));

    UploadPartOptions uploadPartOptions = UploadPartOptions.builder()
        .contentLength(7)
        .data(new ByteArrayInputStream("Robothy".getBytes()))
        .build();
    objectService.uploadPart(bucket, key, uploadId, 1, uploadPartOptions);
    LocalS3Metadata localS3Metadata = objectService.localS3Metadata();
    Optional<BucketMetadata> bucketMetadataOpt = localS3Metadata.getBucketMetadata(bucket);
    assertTrue(bucketMetadataOpt.isPresent());
    UploadMetadata uploadMetadata = UploadAssertions.assertUploadExists(bucketMetadataOpt.get(), key, uploadId);
    UploadPartMetadata uploadPartMetadata1 = uploadMetadata.getParts().get(1);
    assertNotNull(uploadPartMetadata1);
    assertNotEquals(0, uploadPartMetadata1.getFileId());
    assertEquals(7, uploadPartMetadata1.getSize());
    assertTrue(System.currentTimeMillis() - uploadPartMetadata1.getLastModified() < 5000);
  }
}