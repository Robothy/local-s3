package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.datatypes.response.S3Object;
import java.io.ByteArrayInputStream;

import org.apache.commons.codec.digest.DigestUtils;
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

    ByteArrayInputStream content = new ByteArrayInputStream("Hello".getBytes());
    PutObjectOptions putObjectOptions = PutObjectOptions.builder().content(content)
        .contentType("plain/text")
        .size(5).build();
    objectService.putObject(bucketName, key1, putObjectOptions);
    content.reset();
    objectService.putObject(bucketName, key2, putObjectOptions);
    objectService.deleteObject(bucketName, key3);
    content.reset();
    objectService.putObject(bucketName, key4, putObjectOptions);

    ListObjectsAns listObjectsAns = objectService.listObjects(bucketName, null, null, null, 2, null);
    assertEquals(2, listObjectsAns.getObjects().size());

    S3Object dir1Key1 = listObjectsAns.getObjects().get(0);
    assertEquals(DigestUtils.md5Hex("Hello"), dir1Key1.getEtag());
    assertEquals("dir1/key1", dir1Key1.getKey());
    assertEquals(5, dir1Key1.getSize());

    S3Object dir1Key2 = listObjectsAns.getObjects().get(1);
    assertEquals(DigestUtils.md5Hex("Hello"), dir1Key2.getEtag());
    assertEquals("dir1/key2", dir1Key2.getKey());
    assertEquals(5, dir1Key2.getSize());

    assertEquals(0, listObjectsAns.getCommonPrefixes().size());
    assertTrue(listObjectsAns.getNextMarker().isPresent());
    assertEquals("dir1/key2", listObjectsAns.getNextMarker().get());

    ListObjectsAns listObjectsAns1 = objectService.listObjects(bucketName, null, null, "dir1/key2", 2, null);
    assertEquals(1, listObjectsAns1.getObjects().size());
    assertEquals(0, listObjectsAns1.getCommonPrefixes().size());
    assertTrue(listObjectsAns1.getNextMarker().isEmpty());

    ListObjectsAns listObjectsAns2 = objectService.listObjects(bucketName, "/", null, null, 1, null);
    assertEquals(0, listObjectsAns2.getObjects().size());
    assertEquals(1, listObjectsAns2.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns2.getCommonPrefixes().get(0));
    assertTrue(listObjectsAns2.getNextMarker().isPresent());
    assertEquals("dir1/", listObjectsAns2.getNextMarker().get());

    ListObjectsAns listObjectsAns3 = objectService.listObjects(bucketName, "/", null, null, 5, null);
    assertEquals(0, listObjectsAns3.getObjects().size());
    assertEquals(2, listObjectsAns3.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns3.getCommonPrefixes().get(0));
    assertEquals("dir2/", listObjectsAns3.getCommonPrefixes().get(1));
    assertTrue(listObjectsAns3.getNextMarker().isEmpty());

    ListObjectsAns listObjectsAns4 = objectService.listObjects(bucketName, "/", null, null, 5, "dir1");
    assertEquals(0, listObjectsAns4.getObjects().size());
    assertEquals(1, listObjectsAns4.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectsAns4.getCommonPrefixes().get(0));
    assertTrue(listObjectsAns4.getNextMarker().isEmpty());
  }

  @MethodSource("localS3Services")
  @ParameterizedTest
  void listObjectsWithUrlEncoding(BucketService bucketService, ObjectService objectService) {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    String key1 = "dir1@/key1@";
    objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .contentType("plain/text")
        .build());

    assertThrows(LocalS3InvalidArgumentException.class, () ->
        objectService.listObjects(bucketName, null, "invalid", null, 2, null));

    ListObjectsAns listObjectsAns = objectService.listObjects(bucketName, null, "url", null, 2, null);
    assertEquals(1, listObjectsAns.getObjects().size());
    assertEquals("dir1%40/key1%40", listObjectsAns.getObjects().get(0).getKey());

    ListObjectsAns listObjectsAns1 = objectService.listObjects(bucketName, null, null, null, 2, null);
    assertEquals(1, listObjectsAns1.getObjects().size());
    assertEquals("dir1@/key1@", listObjectsAns1.getObjects().get(0).getKey());

    ListObjectsAns listObjectsAns2 = objectService.listObjects(bucketName, "/", "url", null, 2, null);
    assertEquals(0, listObjectsAns2.getObjects().size());
    assertEquals(1, listObjectsAns2.getCommonPrefixes().size());
    assertEquals("dir1%40/", listObjectsAns2.getCommonPrefixes().get(0));
  }



}