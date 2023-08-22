package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.answers.ListPartsAns;
import com.robothy.s3.core.model.answers.UploadPartAns;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.model.request.UploadPartOptions;
import java.io.ByteArrayInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ListPartsServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void listParts(BucketService bucketService, ObjectService objectService) {
    final String bucketName = "test-list-parts";
    bucketService.createBucket(bucketName);
    assertThrows(UploadNotExistException.class, () ->
        objectService.listParts(bucketName, "a.txt", "test-list-parts", 1, 0));

    String uploadId = objectService.createMultipartUpload(bucketName, "a.txt", CreateMultipartUploadOptions.builder()
        .contentType("text/plain")
        .build());
    // no parts uploaded.
    ListPartsAns listPartsAns1 = objectService.listParts(bucketName, "a.txt", uploadId, null, 0);
    assertNotNull(listPartsAns1);
    assertEquals(bucketName, listPartsAns1.getBucket());
    assertEquals("a.txt", listPartsAns1.getKey());
    assertEquals(uploadId, listPartsAns1.getUploadId());
    assertEquals(0, listPartsAns1.getPartNumberMarker());
    assertEquals(0, listPartsAns1.getNextPartNumberMarker());
    assertEquals(1000, listPartsAns1.getMaxParts());
    assertFalse(listPartsAns1.isTruncated());
    assertEquals(0, listPartsAns1.getParts().size());


    UploadPartAns uploadPartAns1 = objectService.uploadPart(bucketName, "a.txt", uploadId, 1, UploadPartOptions.builder()
        .data(new ByteArrayInputStream("Hello".getBytes()))
        .contentLength(5)
        .build());
    // the first part uploaded.
    ListPartsAns listPartsAns2 = objectService.listParts(bucketName, "a.txt", uploadId, null, 0);
    assertEquals(0, listPartsAns2.getPartNumberMarker());
    assertEquals(1, listPartsAns2.getNextPartNumberMarker());
    assertEquals(1000, listPartsAns2.getMaxParts());
    assertFalse(listPartsAns2.isTruncated());
    assertEquals(1, listPartsAns2.getParts().size());
    ListPartsAns.Part fetchedPart1 = listPartsAns2.getParts().get(0);
    assertEquals(DigestUtils.md5Hex("Hello"), fetchedPart1.getETag());
    assertEquals(1, fetchedPart1.getPartNumber());
    assertEquals(5, fetchedPart1.getSize());
    assertTrue(fetchedPart1.getLastModified() > 0 && fetchedPart1.getLastModified() <= System.currentTimeMillis());


    UploadPartAns uploadPartAns2 = objectService.uploadPart(bucketName, "a.txt", uploadId, 2, UploadPartOptions.builder()
        .data(new ByteArrayInputStream("World".getBytes()))
        .contentLength(5)
        .build());
    UploadPartAns uploadPartAns3 = objectService.uploadPart(bucketName, "a.txt", uploadId, 3, UploadPartOptions.builder()
        .data(new ByteArrayInputStream("!".getBytes()))
        .contentLength(1)
        .build());

    // the upload has 3 parts
    // list the first part, set the max-parts to 1
    ListPartsAns listPartsAns3 = objectService.listParts(bucketName, "a.txt", uploadId, 1, 0);
    assertEquals(0, listPartsAns3.getPartNumberMarker());
    assertEquals(1, listPartsAns3.getNextPartNumberMarker());
    assertEquals(1, listPartsAns3.getMaxParts());
    assertTrue(listPartsAns3.isTruncated());
    assertEquals(1, listPartsAns3.getParts().size());
    assertEquals(1, listPartsAns3.getParts().get(0).getPartNumber());

    // list the second part, set the max-parts to 1
    ListPartsAns listPartsAns4 = objectService.listParts(bucketName, "a.txt", uploadId, 1, 1);
    assertEquals(1, listPartsAns4.getPartNumberMarker());
    assertEquals(2, listPartsAns4.getNextPartNumberMarker());
    assertEquals(1, listPartsAns4.getMaxParts());
    assertTrue(listPartsAns4.isTruncated());
    assertEquals(1, listPartsAns4.getParts().size());
    assertEquals(2, listPartsAns4.getParts().get(0).getPartNumber());

    // list the last part, not set the max-parts
    ListPartsAns listPartsAns5 = objectService.listParts(bucketName, "a.txt", uploadId, null, 2);
    assertEquals(2, listPartsAns5.getPartNumberMarker());
    assertEquals(3, listPartsAns5.getNextPartNumberMarker());
    assertEquals(1000, listPartsAns5.getMaxParts());
    assertFalse(listPartsAns5.isTruncated());
    assertEquals(1, listPartsAns5.getParts().size());
    assertEquals(3, listPartsAns5.getParts().get(0).getPartNumber());

    // list the first 2 parts
    ListPartsAns listPartsAns7 = objectService.listParts(bucketName, "a.txt", uploadId, 2, 0);
    assertEquals(0, listPartsAns7.getPartNumberMarker());
    assertEquals(2, listPartsAns7.getNextPartNumberMarker());
    assertEquals(2, listPartsAns7.getMaxParts());
    assertTrue(listPartsAns7.isTruncated());
    assertEquals(2, listPartsAns7.getParts().size());
    assertEquals(1, listPartsAns7.getParts().get(0).getPartNumber());
    assertEquals(2, listPartsAns7.getParts().get(1).getPartNumber());

    // list the last 2 parts
    ListPartsAns listPartsAns8 = objectService.listParts(bucketName, "a.txt", uploadId, 2, 1);
    assertEquals(1, listPartsAns8.getPartNumberMarker());
    assertEquals(3, listPartsAns8.getNextPartNumberMarker());
    assertEquals(2, listPartsAns8.getMaxParts());
    assertFalse(listPartsAns8.isTruncated());
    assertEquals(2, listPartsAns8.getParts().size());
    assertEquals(2, listPartsAns8.getParts().get(0).getPartNumber());
    assertEquals(3, listPartsAns8.getParts().get(1).getPartNumber());

    // list all parts
    ListPartsAns listPartsAns9 = objectService.listParts(bucketName, "a.txt", uploadId, null, 0);
    assertEquals(0, listPartsAns9.getPartNumberMarker());
    assertEquals(3, listPartsAns9.getNextPartNumberMarker());
    assertEquals(1000, listPartsAns9.getMaxParts());
    assertFalse(listPartsAns9.isTruncated());
    assertEquals(3, listPartsAns9.getParts().size());
    assertEquals(1, listPartsAns9.getParts().get(0).getPartNumber());
    assertEquals(2, listPartsAns9.getParts().get(1).getPartNumber());
    assertEquals(3, listPartsAns9.getParts().get(2).getPartNumber());
  }

}