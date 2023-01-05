package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.answers.CompleteMultipartUploadAns;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.CompleteMultipartUploadPartOption;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.UploadPartOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CompleteMultipartUploadServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void completeMultipartUpload(BucketService bucketService, ObjectService objectService) throws IOException {
    String bucket = "my-bucket";
    String key = "a.txt";

    assertThrows(BucketNotExistException.class, () -> objectService.completeMultipartUpload(bucket, key, "123", null));
    bucketService.createBucket(bucket);
    assertThrows(ObjectNotExistException.class, () -> objectService.completeMultipartUpload(bucket, key, "123", null));

    String uploadId = objectService.createMultipartUpload(bucket, key, CreateMultipartUploadOptions.builder()
        .contentType("plain/text")
        .build());
    assertThrows(UploadNotExistException.class, () -> objectService.completeMultipartUpload(bucket, key, "123", Collections.emptyList()));
    assertThrows(IllegalArgumentException.class, () -> objectService.completeMultipartUpload(bucket, key, uploadId, Collections.emptyList()));

    objectService.uploadPart(bucket, key, uploadId, 1, UploadPartOptions.builder()
            .data(new ByteArrayInputStream("Hello".getBytes()))
            .contentLength(5)
        .build());

    objectService.uploadPart(bucket, key, uploadId, 2, UploadPartOptions.builder()
        .data(new ByteArrayInputStream("World".getBytes()))
        .contentLength(5)
        .build());

    CompleteMultipartUploadAns completeAns = objectService.completeMultipartUpload(bucket, key, uploadId, Arrays.asList(
            CompleteMultipartUploadPartOption.builder().partNumber(1).build(),
            CompleteMultipartUploadPartOption.builder().partNumber(2).build()));
    assertEquals("/" + bucket + "/" + key, completeAns.getLocation());
    assertNull(completeAns.getVersionId());

    GetObjectAns object = objectService.getObject(bucket, key, GetObjectOptions.builder().build());
    assertEquals("plain/text", object.getContentType());
    assertEquals("HelloWorld", new String(object.getContent().readAllBytes()));
  }

}