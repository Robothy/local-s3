package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.request.CompleteMultipartUploadPartOption;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.model.request.UploadPartOptions;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AbortMultipartUploadServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void abortMultipartUpload(BucketService bucketService, ObjectService objectService) {
    String bucketName = "test-abort-multipart-upload";
    bucketService.createBucket(bucketName);
    // This behavior is not the same as the AWS S3. AWS S3 will throw NoSuchUpload exception.
    // LocalS3 won't do anything in this scenario because we don't want to store aborted upload ID.
    assertDoesNotThrow(() -> objectService.abortMultipartUpload(bucketName, "a.txt", "test-abort-multipart-upload"));


    String uploadId1 = objectService.createMultipartUpload(bucketName, "a.txt", CreateMultipartUploadOptions.builder()
        .contentType("text/plain")
        .build());
    assertDoesNotThrow(() -> objectService.abortMultipartUpload(bucketName, "a.txt", uploadId1));
    // abort multiple times
    assertDoesNotThrow(() -> objectService.abortMultipartUpload(bucketName, "a.txt", uploadId1));


    String uploadId2 = objectService.createMultipartUpload(bucketName, "a.txt", CreateMultipartUploadOptions.builder()
        .contentType("text/plain")
        .build());
    objectService.uploadPart(bucketName, "a.txt", uploadId2, 1, UploadPartOptions.builder()
        .contentLength(5)
        .data(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    objectService.listParts(bucketName, "a.txt", uploadId2, 0, null);
    objectService.abortMultipartUpload(bucketName, "a.txt", uploadId2);
    assertThrows(UploadNotExistException.class, () ->
        objectService.listParts(bucketName, "a.txt", uploadId2, 0, null));
    assertThrows(UploadNotExistException.class, () ->
        objectService.completeMultipartUpload(bucketName, "a.txt", uploadId2, List.of(CompleteMultipartUploadPartOption.builder()
            .partNumber(1)
            .build())));


    String uploadId3 = objectService.createMultipartUpload(bucketName, "a.txt", CreateMultipartUploadOptions.builder()
        .contentType("text/plain")
        .build());
    objectService.uploadPart(bucketName, "a.txt", uploadId3, 1, UploadPartOptions.builder()
        .contentLength(5)
        .data(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    String uploadId4 = objectService.createMultipartUpload(bucketName, "a.txt", CreateMultipartUploadOptions.builder()
        .contentType("text/plain")
        .build());
    objectService.uploadPart(bucketName, "a.txt", uploadId4, 1, UploadPartOptions.builder()
        .contentLength(5)
        .data(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    assertDoesNotThrow(() -> objectService.abortMultipartUpload(bucketName, "a.txt", uploadId3));
    assertDoesNotThrow(() -> objectService.abortMultipartUpload(bucketName, "a.txt", uploadId3));
  }

}