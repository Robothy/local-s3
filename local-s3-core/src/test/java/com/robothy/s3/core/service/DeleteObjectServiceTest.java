package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DeleteObjectServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void deleteObject(BucketService bucketService, ObjectService objectService) {
    String bucketName = "my-bucket";
    String key = "key";
    bucketService.createBucket(bucketName);
    objectService.putObject(bucketName, key, PutObjectOptions.builder()
        .contentType("application/xml")
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .size(5)
        .build());

    /*-- bucket versioning is disabled --*/
    // delete without version ID
    DeleteObjectAns deleteObjectAns = objectService.deleteObject(bucketName, key);
    assertTrue(deleteObjectAns.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns.getVersionId());
    GetObjectAns getObjectAns = objectService.getObject(bucketName, key, GetObjectOptions.builder()
        .versionId(deleteObjectAns.getVersionId()).build());
    assertTrue(getObjectAns.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns.getVersionId());
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(bucketService.localS3Metadata(), bucketName);
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    assertEquals(1, objectMetadata.getVersionedObjectMap().size());

    // delete with incorrect version ID
    assertDoesNotThrow(() -> objectService.deleteObject(bucketName, key, "incorrect-version-id"));
    objectService.deleteObject(bucketName, key, ObjectMetadata.NULL_VERSION);
    assertThrows(ObjectNotExistException.class, () -> objectService.deleteObject(bucketName, key, ObjectMetadata.NULL_VERSION));
    BucketAssertions.assertBucketIsEmpty(bucketMetadata);

    /*-- bucket versioning is enabled --*/
    bucketService.setVersioningEnabled(bucketName, true);
    DeleteObjectAns deleteObjectAns1 = objectService.deleteObject(bucketName, key);
    objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key);
    assertTrue(deleteObjectAns1.isDeleteMarker());
    assertNotEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns1.getVersionId());
    GetObjectAns getObjectAns1 = objectService.getObject(bucketName, key, GetObjectOptions.builder()
        .versionId(deleteObjectAns1.getVersionId()).build());
    assertTrue(getObjectAns1.isDeleteMarker());
    assertEquals(deleteObjectAns1.getVersionId(), getObjectAns1.getVersionId());

    DeleteObjectAns deleteObjectAns2 = objectService.deleteObject(bucketName, key);
    assertTrue(deleteObjectAns2.isDeleteMarker());
    assertNotEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns2.getVersionId());
    GetObjectAns getObjectAns2 = objectService.getObject(bucketName, key, GetObjectOptions.builder()
        .versionId(deleteObjectAns2.getVersionId()).build());
    assertTrue(getObjectAns2.isDeleteMarker());
    assertEquals(deleteObjectAns2.getVersionId(), getObjectAns2.getVersionId());

    PutObjectAns putObjectAns1 = objectService.putObject(bucketName, key, PutObjectOptions.builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("Robothy".getBytes()))
        .size(7)
        .build());
    assertEquals(3, objectMetadata.getVersionedObjectMap().size());

    DeleteObjectAns deleteObjectAns3 = objectService.deleteObject(bucketName, key, deleteObjectAns2.getVersionId());
    assertTrue(deleteObjectAns3.isDeleteMarker());
    assertEquals(deleteObjectAns2.getVersionId(), deleteObjectAns3.getVersionId());

    DeleteObjectAns deleteObjectAns4 = objectService.deleteObject(bucketName, key, putObjectAns1.getVersionId());
    assertFalse(deleteObjectAns4.isDeleteMarker());
    assertEquals(putObjectAns1.getVersionId(), deleteObjectAns4.getVersionId());
    assertEquals(1, bucketMetadata.getObjectMap().size());

    /* disable bucket versioning */
    bucketService.setVersioningEnabled(bucketName, false);
    DeleteObjectAns deleteObjectAns5 = objectService.deleteObject(bucketName, key);
    assertTrue(deleteObjectAns5.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns5.getVersionId());
    assertEquals(2, objectMetadata.getVersionedObjectMap().size());

    DeleteObjectAns deleteObjectAns6 = objectService.deleteObject(bucketName, key);
    assertTrue(deleteObjectAns6.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns6.getVersionId());
    assertEquals(2, objectMetadata.getVersionedObjectMap().size());

    DeleteObjectAns deleteObjectAns9 = objectService.deleteObject(bucketName, key, ObjectMetadata.NULL_VERSION);
    assertTrue(deleteObjectAns9.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns9.getVersionId());
    assertEquals(1, objectMetadata.getVersionedObjectMap().size());

    DeleteObjectAns deleteObjectAns10 = objectService.deleteObject(bucketName, key, deleteObjectAns1.getVersionId());
    assertTrue(deleteObjectAns10.isDeleteMarker());
    assertEquals(deleteObjectAns1.getVersionId(), deleteObjectAns10.getVersionId());
    BucketAssertions.assertBucketIsEmpty(bucketMetadata);

    String key1 = "key1";
    DeleteObjectAns deleteObjectAns7 = objectService.deleteObject(bucketName, key1);
    assertTrue(deleteObjectAns7.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleteObjectAns7.getVersionId());

    PutObjectAns putObjectAns = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("Robothy".getBytes()))
        .size(7)
        .build());
    DeleteObjectAns deleteObjectAns8 = objectService.deleteObject(bucketName, key1, putObjectAns.getVersionId());
    assertFalse(deleteObjectAns8.isDeleteMarker());
    assertEquals(putObjectAns.getVersionId(), deleteObjectAns8.getVersionId());
  }
}