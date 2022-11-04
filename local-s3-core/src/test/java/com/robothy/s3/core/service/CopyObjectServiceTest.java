package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.answers.CopyObjectAns;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.ListObjectVersionsAns;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.CopyObjectOptions;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.datatypes.response.ObjectVersion;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CopyObjectServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void copyObject(BucketService bucketService, ObjectService objectService) {
    String bucket1 = "my-bucket";
    String key1 = "key1";
    String bucket2 = "my-bucket2";
    String key2 = "key2";

    bucketService.createBucket(bucket1);
    bucketService.createBucket(bucket2);
    String text1 = "Hello";
    objectService.putObject(bucket1, key1, PutObjectOptions.builder()
        .size(text1.length())
        .contentType("plain/text")
        .content(new ByteArrayInputStream(text1.getBytes()))
        .build());

    CopyObjectAns copyObjectAns1 = objectService.copyObject(bucket2, key2, CopyObjectOptions.builder()
        .sourceBucket(bucket1)
        .sourceKey(key1)
        .build());
    assertEquals(ObjectMetadata.NULL_VERSION, copyObjectAns1.getVersionId());
    assertEquals(ObjectMetadata.NULL_VERSION, copyObjectAns1.getSourceVersionId());
    assertTrue(System.currentTimeMillis() - copyObjectAns1.getLastModified() < 5000);
    GetObjectAns object1 = objectService.getObject(bucket2, key2, GetObjectOptions.builder().build());
    assertEquals(text1.length(), object1.getSize());
    assertEquals("plain/text", object1.getContentType());
    assertEquals(copyObjectAns1.getVersionId(), object1.getVersionId());

    bucketService.setVersioningEnabled(bucket1, true);
    CopyObjectAns copyObjectAns2 = objectService.copyObject(bucket1, key1, CopyObjectOptions.builder()
        .sourceBucket(bucket2).sourceKey(key2).build());
    assertEquals(ObjectMetadata.NULL_VERSION, copyObjectAns2.getSourceVersionId());
    assertNotEquals(ObjectMetadata.NULL_VERSION, copyObjectAns2.getVersionId());
    ListObjectVersionsAns versions1 = objectService.listObjectVersions(bucket1, null, null, 1000, key1, null);
    assertEquals(2, versions1.getVersions().size());
    assertEquals(copyObjectAns2.getVersionId(), ((ObjectVersion)versions1.getVersions().get(0)).getVersionId());
    assertEquals(ObjectMetadata.NULL_VERSION, ((ObjectVersion)versions1.getVersions().get(1)).getVersionId());

    // Cannot copy a delete marker
    DeleteObjectAns deleteObjectAns = objectService.deleteObject(bucket1, key1);
    assertThrows(IllegalArgumentException.class, () -> objectService.copyObject(bucket2, key2, CopyObjectOptions.builder()
        .sourceBucket(bucket1)
        .sourceKey(key1)
        .sourceVersion(deleteObjectAns.getVersionId())
        .build()));

    CopyObjectAns copyObjectAns3 = objectService.copyObject(bucket2, key2, CopyObjectOptions.builder()
        .sourceBucket(bucket1)
        .sourceKey(key1)
        .sourceVersion(copyObjectAns2.getVersionId())
        .build());
    assertEquals(copyObjectAns2.getVersionId(), copyObjectAns3.getSourceVersionId());
    assertEquals(ObjectMetadata.NULL_VERSION, copyObjectAns3.getVersionId());
  }

}