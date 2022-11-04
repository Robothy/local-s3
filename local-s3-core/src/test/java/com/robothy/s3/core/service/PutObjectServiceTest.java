package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.service.manager.LocalS3Manager;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PutObjectServiceTest extends LocalS3ServiceTestBase {


  @MethodSource("localS3Managers")
  @ParameterizedTest
  void putObject(LocalS3Manager manager) {
    BucketService bucketService = manager.bucketService();
    ObjectService objectService = manager.objectService();
    String bucketName = "my-bucket";

    bucketService.createBucket(bucketName);

    /*-- Bucket versioning not enabled. --*/
    assertNotEquals(Boolean.TRUE, bucketService.getVersioningEnabled(bucketName));

    String key1 = "key1";
    PutObjectAns putObjectAns1 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .contentType("plain/text")
        .size(5)
        .build());
    assertEquals(key1, putObjectAns1.getKey());
    assertEquals(ObjectMetadata.NULL_VERSION, putObjectAns1.getVersionId());

    LocalS3Metadata s3Metadata = objectService.localS3Metadata();
    Optional<BucketMetadata> bucketMetadataOpt = s3Metadata.getBucketMetadata(bucketName);
    assertTrue(bucketMetadataOpt.isPresent());
    BucketMetadata bucketMetadata = bucketMetadataOpt.get();
    Optional<ObjectMetadata> objectMetadata1Opt = bucketMetadata.getObjectMetadata(key1);
    assertTrue(objectMetadata1Opt.isPresent());
    ObjectMetadata objectMetadata1 = objectMetadata1Opt.get();
    Optional<String> virtualVersion1Opt = objectMetadata1.getVirtualVersion();
    assertTrue(virtualVersion1Opt.isPresent());

    String virtualVersion1 = virtualVersion1Opt.get();
    assertEquals(virtualVersion1, objectMetadata1.getLatestVersion());
    Optional<VersionedObjectMetadata> versionedObjectMetadata1Opt =
        objectMetadata1.getVersionedObjectMetadata(virtualVersion1);
    assertTrue(versionedObjectMetadata1Opt.isPresent());
    VersionedObjectMetadata versionedObjectMetadata1 = versionedObjectMetadata1Opt.get();
    assertEquals("plain/text", versionedObjectMetadata1.getContentType());
    assertEquals(5, versionedObjectMetadata1.getSize());

    PutObjectAns putObjectAns2 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .contentType("application/json")
        .size(10)
        .content(new ByteArrayInputStream("{\"length\": 12}".getBytes()))
        .build());
    assertEquals(key1, putObjectAns2.getKey());
    assertEquals(ObjectMetadata.NULL_VERSION, putObjectAns2.getVersionId());
    Optional<String> virtualVersion2Opt = objectMetadata1.getVirtualVersion();
    assertTrue(virtualVersion2Opt.isPresent());
    String virtualVersion2 = virtualVersion2Opt.get();
    assertEquals(virtualVersion2, objectMetadata1.getLatestVersion());
    assertEquals(1, objectMetadata1.getVersionedObjectMap().size());


    /*-- Enable bucket versioning. --*/
    bucketService.setVersioningEnabled(bucketName, true);
    PutObjectAns putObjectAns3 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    assertEquals(key1, putObjectAns3.getKey());
    assertNotNull(putObjectAns3.getVersionId());
    assertNotEquals(ObjectMetadata.NULL_VERSION, putObjectAns3.getVersionId());
    assertEquals(putObjectAns3.getVersionId(), objectMetadata1.getLatestVersion());
    Optional<String> virtualVersion3Opt = objectMetadata1.getVirtualVersion();
    assertTrue(virtualVersion3Opt.isPresent());
    assertNotEquals(objectMetadata1.getLatestVersion(), virtualVersion3Opt.get());
    Optional<VersionedObjectMetadata> versionedObjectMetadataOpt =
        objectMetadata1.getVersionedObjectMetadata(putObjectAns3.getVersionId());
    assertTrue(versionedObjectMetadataOpt.isPresent());
    assertEquals(objectMetadata1.getLatest(), versionedObjectMetadataOpt.get());
    assertEquals(2, objectMetadata1.getVersionedObjectMap().size());


    /*-- Disable bucket versioning --*/
    bucketService.setVersioningEnabled(bucketName, false);
    PutObjectAns putObjectAns4 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("World".getBytes()))
        .contentType("application/xml")
        .build());
    assertEquals(key1, putObjectAns4.getKey());
    assertEquals(ObjectMetadata.NULL_VERSION, putObjectAns4.getVersionId());
    Optional<String> virtualVersion4Opt = objectMetadata1.getVirtualVersion();
    assertTrue(virtualVersion4Opt.isPresent());
    assertEquals(objectMetadata1.getLatestVersion(), virtualVersion4Opt.get());
    assertEquals(2, objectMetadata1.getVersionedObjectMap().size());
  }

}