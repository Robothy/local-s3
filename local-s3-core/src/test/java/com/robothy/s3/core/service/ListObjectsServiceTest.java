package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ListObjectsServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void list(BucketService bucketService, ObjectService objectService) {

    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    String key1 = "dir1/key1";
    String key2 = "dir1/key2";
    String key3 = "dir2/key3";
    String key4 = "dir2/key4";

    PutObjectOptions putObjectOptions = PutObjectOptions.builder().content(new ByteArrayInputStream("Hello".getBytes()))
        .contentType("plain/text")
        .size(5).build();
    objectService.putObject(bucketName, key1, putObjectOptions);
    objectService.putObject(bucketName, key2, putObjectOptions);
    objectService.deleteObject(bucketName, key3);
    objectService.putObject(bucketName, key4, putObjectOptions);

    ListObjectsAns listObjectsAns = objectService.listObjects(bucketName, null, null, 2, null);
    assertEquals(2, listObjectsAns.getObjects().size());
    assertEquals(0, listObjectsAns.getCommonPrefixes().size());
    assertTrue(listObjectsAns.getNextMarker().isPresent());
    assertEquals("dir1/key2", listObjectsAns.getNextMarker().get());

    ListObjectsAns listObjectsAns1 = objectService.listObjects(bucketName, null, "dir1/key2", 2, null);
    assertEquals(1, listObjectsAns1.getObjects().size());
    assertEquals(0, listObjectsAns1.getCommonPrefixes().size());
    assertTrue(listObjectsAns1.getNextMarker().isEmpty());

    ListObjectsAns listObjectsAns2 = objectService.listObjects(bucketName, '/', null, 1, null);
    assertEquals(0, listObjectsAns2.getObjects().size());
    assertEquals(1, listObjectsAns2.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns2.getCommonPrefixes().get(0));
    assertTrue(listObjectsAns2.getNextMarker().isPresent());
    assertEquals("dir1/key1", listObjectsAns2.getNextMarker().get());

    ListObjectsAns listObjectsAns3 = objectService.listObjects(bucketName, '/', null, 5, null);
    assertEquals(0, listObjectsAns3.getObjects().size());
    assertEquals(2, listObjectsAns3.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns3.getCommonPrefixes().get(0));
    assertEquals("dir2/", listObjectsAns3.getCommonPrefixes().get(1));
    assertTrue(listObjectsAns3.getNextMarker().isEmpty());

    ListObjectsAns listObjectsAns4 = objectService.listObjects(bucketName, '/', null, 5, "dir1");
    assertEquals(0, listObjectsAns4.getObjects().size());
    assertEquals(1, listObjectsAns4.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns4.getCommonPrefixes().get(0));
    assertTrue(listObjectsAns4.getNextMarker().isEmpty());
  }

}