package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.VersionedObjectNotExistException;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GetObjectServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void getObject(BucketService bucketService, ObjectService objectService) throws IOException {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    assertThrows(ObjectNotExistException.class, () -> objectService.getObject(bucketName, "not-exists-key",
        GetObjectOptions.builder().build()));

    /*-- Bucket versioning not enabled. --*/
    String key1 = "key1";
    // Put first version of key1
    objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .contentType("plain/text")
        .size(6)
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    LocalS3Metadata localS3Metadata = bucketService.localS3Metadata();
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata, bucketName);
    ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, key1);
    Optional<String> virtualVersionOpt = objectMetadata.getVirtualVersion();
    assertTrue(virtualVersionOpt.isPresent());
    String virtualVersion = virtualVersionOpt.get();
    assertThrows(VersionedObjectNotExistException.class, () -> objectService.getObject(bucketName, key1,
        GetObjectOptions.builder().versionId(virtualVersion).build()));
    GetObjectAns getObjectAns = objectService.getObject(bucketName, key1, GetObjectOptions.builder()
        .versionId(ObjectMetadata.NULL_VERSION).build());
    assertEquals(key1, getObjectAns.getKey());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns.getVersionId());
    assertEquals("plain/text", getObjectAns.getContentType());
    assertEquals("Hello", new String(getObjectAns.getContent().readAllBytes()));
    assertFalse(getObjectAns.isDeleteMarker());

    // Get first version of key1 without specify version ID.
    GetObjectAns getObjectAns1 = objectService.getObject(bucketName, key1, GetObjectOptions.builder().build());
    assertFalse(getObjectAns1.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns1.getVersionId());
    assertEquals("plain/text", getObjectAns1.getContentType());
    assertEquals("Hello", new String(getObjectAns1.getContent().readAllBytes()));

    // Put second version of key1
    objectService.putObject(bucketName, key1, PutObjectOptions.builder()
            .contentType("application/xml")
            .size(7)
            .content(new ByteArrayInputStream("Robothy".getBytes()))
        .build());
    // Get second version of key1 with version ID.
    GetObjectAns getObjectAns2 = objectService.getObject(bucketName, key1, GetObjectOptions.builder()
        .versionId(ObjectMetadata.NULL_VERSION).build());
    assertFalse(getObjectAns2.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns2.getVersionId());
    assertEquals("application/xml", getObjectAns2.getContentType());
    assertEquals("Robothy", new String(getObjectAns2.getContent().readAllBytes()));

    // Get second version of key1 without version ID.
    GetObjectAns getObjectAns3 = objectService.getObject(bucketName, key1, GetObjectOptions.builder().build());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns3.getVersionId());
    assertFalse(getObjectAns3.isDeleteMarker());
    assertEquals("application/xml", getObjectAns3.getContentType());
    assertEquals("Robothy", new String(getObjectAns3.getContent().readAllBytes()));

    /*-- Enable bucket versioning --*/
    bucketService.setVersioningEnabled(bucketName, true);

    // Put third version of key1
    PutObjectAns putObjectAns = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .contentType("application/json")
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .size(6)
        .build());

    // Get third version of key1 with version ID.
    GetObjectAns getObjectAns4 = objectService.getObject(bucketName, key1, GetObjectOptions.builder()
        .versionId(putObjectAns.getVersionId()).build());
    assertFalse(getObjectAns4.isDeleteMarker());
    assertEquals("application/json", getObjectAns4.getContentType());
    assertEquals("Hello", new String(getObjectAns4.getContent().readAllBytes()));
    assertEquals(putObjectAns.getVersionId(), getObjectAns4.getVersionId());

    // Get third version of key1 without version ID.
    GetObjectAns getObjectAns5 = objectService.getObject(bucketName, key1, GetObjectOptions.builder().build());
    assertFalse(getObjectAns5.isDeleteMarker());
    assertEquals("application/json", getObjectAns5.getContentType());
    assertEquals("Hello", new String(getObjectAns5.getContent().readAllBytes()));
    assertEquals(putObjectAns.getVersionId(), getObjectAns5.getVersionId());

    /*-- Disable bucket versioning --*/
    bucketService.setVersioningEnabled(bucketName, false);
    GetObjectAns getObjectAns6 = objectService.getObject(bucketName, key1, GetObjectOptions.builder().build());
    assertFalse(getObjectAns6.isDeleteMarker());
    assertEquals("application/json", getObjectAns6.getContentType());
    assertEquals("Hello", new String(getObjectAns6.getContent().readAllBytes()));
    assertEquals(putObjectAns.getVersionId(), getObjectAns6.getVersionId());

    // Get delete marker
    DeleteObjectAns deleteObjectAns = objectService.deleteObject(bucketName, key1, null);
    GetObjectAns getObjectAns7 = objectService.getObject(bucketName, key1, GetObjectOptions.builder()
        .versionId(deleteObjectAns.getVersionId()).build());
    assertTrue(getObjectAns7.isDeleteMarker());
    assertNull(getObjectAns7.getContentType());
    assertEquals(ObjectMetadata.NULL_VERSION, getObjectAns7.getVersionId());

    GetObjectAns getObjectAns8 = objectService.headObject(bucketName, key1, GetObjectOptions.builder()
        .versionId(deleteObjectAns.getVersionId()).build());
    assertNull(getObjectAns8.getContent());

    assertThrows(ObjectNotExistException.class, () ->
        objectService.getObject(bucketName, key1, GetObjectOptions.builder().build()));
    assertThrows(ObjectNotExistException.class, () ->
        objectService.headObject(bucketName, key1, GetObjectOptions.builder().build()));
  }

}