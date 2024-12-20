package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.robothy.s3.jupiter.LocalS3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

public class ObjectIntegrationTest {

  @Test
  @LocalS3
  void testGetObject(AmazonS3 s3) throws IOException {
    String bucket = "my-bucket";
    s3.createBucket(bucket);

    String key = "a.txt";
    s3.putObject(bucket, key, "Text1");
    S3Object object = s3.getObject(bucket, key);
    assertEquals("null", object.getObjectMetadata().getVersionId());
    assertEquals("Text1", new String(object.getObjectContent().readAllBytes()));
    assertEquals(DigestUtils.md5Hex("Text1"), object.getObjectMetadata().getETag());

    s3.putObject(bucket, key, "Text2");
    S3Object object1 = s3.getObject(bucket, key);
    assertEquals("null", object1.getObjectMetadata().getVersionId());
    assertEquals("Text2", new String(object1.getObjectContent().readAllBytes()));

    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket, new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    PutObjectResult putObjectResult = s3.putObject(bucket, key, "Text3");
    assertNotNull(putObjectResult.getVersionId());
    S3Object object2 = s3.getObject(bucket, key);
    assertEquals(putObjectResult.getVersionId(), object2.getObjectMetadata().getVersionId());
    assertEquals("Text3", new String(object2.getObjectContent().readAllBytes()));

    PutObjectResult putObjectResult1 = s3.putObject(bucket, key, "Text4");
    assertNotNull(putObjectResult.getVersionId());
    S3Object object3 = s3.getObject(bucket, key);
    assertEquals(putObjectResult1.getVersionId(), object3.getObjectMetadata().getVersionId());
    assertEquals("Text4".length(), object3.getObjectMetadata().getContentLength());
    assertEquals("Text4", new String(object3.getObjectContent().readAllBytes()));

    S3Object object4 = s3.getObject(new GetObjectRequest(bucket, key, putObjectResult.getVersionId()));
    assertEquals(putObjectResult.getVersionId(), object4.getObjectMetadata().getVersionId());
    assertEquals("Text3", new String(object4.getObjectContent().readAllBytes()));

    ObjectMetadata objectMetadata = s3.getObjectMetadata(bucket, key);
    assertEquals("Text4".length(), objectMetadata.getContentLength());
    assertEquals(putObjectResult1.getVersionId(), objectMetadata.getVersionId());

    s3.deleteObject(bucket, key);
    assertThrows(AmazonS3Exception.class, () -> s3.getObject(bucket, key));
    assertThrows(AmazonS3Exception.class, () -> s3.getObjectMetadata(bucket, key));
  }

  @Test
  @LocalS3
  void listObjectVersions(AmazonS3 s3) {
    String bucket = "my-bucket";
    s3.createBucket(bucket);
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket,
        new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    PutObjectResult putObjectResult1 = s3.putObject(bucket, "dir1/key1", "Text1");
    PutObjectResult putObjectResult2 = s3.putObject(bucket, "dir1/key1", "Text2");
    s3.deleteObject(bucket, "dir1/key1");
    PutObjectResult putObjectResult3 = s3.putObject(bucket, "dir1/key1", "Text3");


    VersionListing versionListing1 = s3.listVersions(bucket, "dir1/key1");
    assertEquals(4, versionListing1.getVersionSummaries().size());

    assertEquals(DigestUtils.md5Hex("Text3"), versionListing1.getVersionSummaries().get(0).getETag());
    assertTrue(versionListing1.getVersionSummaries().get(0).isLatest());
    assertEquals("dir1/key1", versionListing1.getVersionSummaries().get(0).getKey());
    assertEquals(5, versionListing1.getVersionSummaries().get(0).getSize());
    assertEquals(putObjectResult3.getVersionId(), versionListing1.getVersionSummaries().get(0).getVersionId());

    assertTrue(versionListing1.getVersionSummaries().get(1).isDeleteMarker());
    assertEquals("dir1/key1", versionListing1.getVersionSummaries().get(1).getKey());
    assertFalse(versionListing1.getVersionSummaries().get(1).isLatest());

    assertEquals(DigestUtils.md5Hex("Text2"), versionListing1.getVersionSummaries().get(2).getETag());
    assertFalse(versionListing1.getVersionSummaries().get(2).isLatest());
    assertEquals("dir1/key1", versionListing1.getVersionSummaries().get(2).getKey());
    assertEquals(5, versionListing1.getVersionSummaries().get(2).getSize());
    assertEquals(putObjectResult2.getVersionId(), versionListing1.getVersionSummaries().get(2).getVersionId());

    assertEquals(DigestUtils.md5Hex("Text1"), versionListing1.getVersionSummaries().get(3).getETag());
    assertFalse(versionListing1.getVersionSummaries().get(3).isLatest());
    assertEquals("dir1/key1", versionListing1.getVersionSummaries().get(3).getKey());
    assertEquals(5, versionListing1.getVersionSummaries().get(3).getSize());
    assertEquals(putObjectResult1.getVersionId(), versionListing1.getVersionSummaries().get(3).getVersionId());

    assertNull(versionListing1.getNextKeyMarker());
    assertTrue(StringUtils.isBlank(versionListing1.getNextVersionIdMarker()));

    PutObjectResult putObjectResult4 = s3.putObject(bucket, "dir2/key1", "Text4");
    PutObjectResult putObjectResult5 = s3.putObject(bucket, "dir2/key1", "Text5");
    s3.deleteObject(bucket, "dir2/key1");
    PutObjectResult putObjectResult6 = s3.putObject(bucket, "dir2/key1", "Text6");
    VersionListing versionListing2 = s3.listVersions(bucket, null, null, null, "/", 1);
    assertEquals(0, versionListing2.getVersionSummaries().size());
    assertEquals(1, versionListing2.getCommonPrefixes().size());
    assertEquals("dir1/", versionListing2.getCommonPrefixes().get(0));
    assertEquals("dir1/key1", versionListing2.getNextKeyMarker());
    assertTrue(StringUtils.isBlank(versionListing2.getNextVersionIdMarker()));

    VersionListing versionListing3 = s3.listVersions(bucket, "dir2", null, null, "/", 1);
    assertEquals(0, versionListing3.getVersionSummaries().size());
    assertEquals(1, versionListing3.getCommonPrefixes().size());
    assertEquals("dir2/", versionListing3.getCommonPrefixes().get(0));
    assertEquals("dir2/key1", versionListing3.getNextKeyMarker());
    assertTrue(StringUtils.isBlank(versionListing3.getNextVersionIdMarker()));

    VersionListing versionListing4 = s3.listVersions(bucket, null, null, null, "/", 2);
    assertEquals(0, versionListing4.getVersionSummaries().size());
    assertEquals(2, versionListing4.getCommonPrefixes().size());
    assertEquals("dir1/", versionListing4.getCommonPrefixes().get(0));
    assertEquals("dir2/", versionListing4.getCommonPrefixes().get(1));


    List<S3VersionSummary> allVersions = new ArrayList<>(8);
    String nextKeyMarker = null;
    String nextVersionIdMarker = null;
    do {
      VersionListing versionListing = s3.listVersions(bucket, null, nextKeyMarker, nextVersionIdMarker, null, 2);
      allVersions.addAll(versionListing.getVersionSummaries());
      nextKeyMarker = versionListing.getNextKeyMarker();
      nextVersionIdMarker = versionListing.getNextVersionIdMarker();
    } while (Objects.nonNull(nextKeyMarker));

    assertEquals(8, allVersions.size());

    assertEquals("dir1/key1", allVersions.get(0).getKey());
    assertEquals(putObjectResult3.getVersionId(), allVersions.get(0).getVersionId());
    assertTrue(allVersions.get(0).isLatest());

    assertEquals("dir1/key1", allVersions.get(1).getKey());
    assertTrue(allVersions.get(1).isDeleteMarker());
    assertFalse(allVersions.get(1).isLatest());

    assertEquals("dir1/key1", allVersions.get(2).getKey());
    assertEquals(putObjectResult2.getVersionId(), allVersions.get(2).getVersionId());
    assertFalse(allVersions.get(2).isLatest());

    assertEquals("dir1/key1", allVersions.get(3).getKey());
    assertEquals(putObjectResult1.getVersionId(), allVersions.get(3).getVersionId());
    assertFalse(allVersions.get(3).isLatest());

    assertEquals("dir2/key1", allVersions.get(4).getKey());
    assertEquals(putObjectResult6.getVersionId(), allVersions.get(4).getVersionId());
    assertTrue(allVersions.get(4).isLatest());

    assertEquals("dir2/key1", allVersions.get(5).getKey());
    assertTrue(allVersions.get(5).isDeleteMarker());
    assertFalse(allVersions.get(5).isLatest());

    assertEquals("dir2/key1", allVersions.get(6).getKey());
    assertEquals(putObjectResult5.getVersionId(), allVersions.get(6).getVersionId());
    assertFalse(allVersions.get(6).isLatest());

    assertEquals("dir2/key1", allVersions.get(7).getKey());
    assertEquals(putObjectResult4.getVersionId(), allVersions.get(7).getVersionId());
    assertFalse(allVersions.get(7).isLatest());
  }

  @LocalS3
  @Test
  void testDeleteObjects(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucketName,
        new BucketVersioningConfiguration(BucketVersioningConfiguration.SUSPENDED)));
    assertDoesNotThrow(() -> s3.deleteObjects(new DeleteObjectsRequest(bucketName)));

    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
    deleteObjectsRequest.setKeys(List.of(
        new DeleteObjectsRequest.KeyVersion("a.txt"),
        new DeleteObjectsRequest.KeyVersion("b.txt", "123")));

    assertThrows(MultiObjectDeleteException.class, () -> s3.deleteObjects(deleteObjectsRequest));

    s3.putObject(bucketName, "a.txt", "Hello");
    s3.putObject(bucketName, "b.txt", "World");
    DeleteObjectsRequest deleteObjectsRequest1 = new DeleteObjectsRequest(bucketName);
    deleteObjectsRequest1.setKeys(List.of(new DeleteObjectsRequest.KeyVersion("a.txt"),
        new DeleteObjectsRequest.KeyVersion("b.txt")));
    DeleteObjectsResult deleteObjectsResult1 = s3.deleteObjects(deleteObjectsRequest1);
    assertEquals(2, deleteObjectsResult1.getDeletedObjects().size());

    VersionListing versionListing1 = s3.listVersions(bucketName, "a.txt");
    assertEquals(1, versionListing1.getVersionSummaries().size());
    assertTrue(versionListing1.getVersionSummaries().get(0).isDeleteMarker());

    VersionListing versionListing2 = s3.listVersions(bucketName, "b.txt");
    assertEquals(1, versionListing2.getVersionSummaries().size());
    assertTrue(versionListing2.getVersionSummaries().get(0).isDeleteMarker());

    DeleteObjectsRequest deleteObjectsRequest2 = new DeleteObjectsRequest(bucketName);
    deleteObjectsRequest2.setKeys(List.of(new DeleteObjectsRequest.KeyVersion("a.txt", "null"),
        new DeleteObjectsRequest.KeyVersion("b.txt", "null")));
    DeleteObjectsResult deleteObjectsResult2 = s3.deleteObjects(deleteObjectsRequest2);
    assertEquals(2, deleteObjectsResult2.getDeletedObjects().size());
    assertTrue(deleteObjectsResult2.getDeletedObjects().get(0).isDeleteMarker());
    assertTrue(deleteObjectsResult2.getDeletedObjects().get(1).isDeleteMarker());

    VersionListing versionListing3 = s3.listVersions(bucketName, "a.txt");
    assertEquals(0, versionListing3.getVersionSummaries().size());
    VersionListing versionListing4 = s3.listVersions(bucketName, "b.txt");
    assertEquals(0, versionListing4.getVersionSummaries().size());
  }

}
