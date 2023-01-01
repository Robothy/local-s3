package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingResult;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.VersionListing;
import com.robothy.s3.jupiter.LocalS3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

public class ObjectIntegrationTest {

  @Test
  @LocalS3
  void testPutObject(AmazonS3 s3) throws IOException {
    Bucket bucket1 = s3.createBucket("bucket1");
    assertFalse(s3.doesObjectExist("bucket1", "hello.txt"));
    s3.putObject(bucket1.getName(), "hello.txt", "Hello");
    assertTrue(s3.doesObjectExist("bucket1", "hello.txt"));
    S3Object object = s3.getObject(bucket1.getName(), "hello.txt");
    assertArrayEquals("Hello".getBytes(), object.getObjectContent().readAllBytes());
  }

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
  void listObjects(AmazonS3 s3) {
    String bucket = "my-bucket";
    String key = "a.txt";
    s3.createBucket(bucket);
    s3.putObject(bucket, key, "Text1");
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket,
        new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    s3.putObject(bucket, key, "Text2");
    ObjectListing objectListing = s3.listObjects(bucket);
    assertEquals(1, objectListing.getObjectSummaries().size());

    s3.putObject(bucket, "dir1/a.txt", "Text3");
    s3.putObject(bucket, "dir1/b.txt", "Text4");

    ObjectListing objectListing1 = s3.listObjects(bucket);
    assertEquals(3, objectListing1.getObjectSummaries().size());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest(bucket, null, null, "/", 10));
    assertEquals(1, objectListing2.getObjectSummaries().size());
    assertEquals(1, objectListing2.getCommonPrefixes().size());
    assertEquals(10, objectListing2.getMaxKeys());

    s3.deleteObject(bucket, key);
    ObjectListing objectListing3 = s3.listObjects(bucket);
    assertEquals(2, objectListing3.getObjectSummaries().size());
    assertEquals(0, objectListing3.getCommonPrefixes().size());
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
    assertEquals(putObjectResult3.getVersionId(), versionListing1.getVersionSummaries().get(0).getVersionId());
    assertTrue(versionListing1.getVersionSummaries().get(0).isLatest());
    assertTrue(versionListing1.getVersionSummaries().get(1).isDeleteMarker());
    assertEquals(putObjectResult2.getVersionId(), versionListing1.getVersionSummaries().get(2).getVersionId());
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
  void testCopyObject(AmazonS3 s3) throws IOException {
    String bucket1 = "bucket1";
    String key1 = "key1";
    String bucket2 = "bucket2";
    String key2 = "key2";
    String text = "Robothy";

    s3.createBucket(bucket1);
    s3.putObject(bucket1, key1, text);
    s3.createBucket(bucket2);

    /*-- Copy from a versioning disabled bucket to another versioning disabled bucket. --*/
    CopyObjectResult copyObjectResult1 = s3.copyObject(bucket1, key1, bucket2, key2);
    assertEquals("null", copyObjectResult1.getVersionId());
    assertTrue(copyObjectResult1.getLastModifiedDate().before(new Date()));
    S3Object object1 = s3.getObject(bucket2, key2);
    assertEquals(text.length(), object1.getObjectMetadata().getContentLength());
    assertEquals(text, new String(object1.getObjectContent().readAllBytes()));

    /*-- Copy from a versioning disabled bucket to a versioning enabled bucket. --*/
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket2, new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    CopyObjectResult copyObjectResult2 = s3.copyObject(bucket1, key1, bucket2, key2);
    assertNotEquals("null", copyObjectResult2.getVersionId());
    assertTrue(copyObjectResult2.getLastModifiedDate().after(copyObjectResult1.getLastModifiedDate()));
    S3Object object2 = s3.getObject(bucket2, key2);
    assertEquals(text.length(), object2.getObjectMetadata().getContentLength());
    assertEquals(text, new String(object2.getObjectContent().readAllBytes()));
    VersionListing versionListing1 = s3.listVersions(bucket2, key2);
    assertEquals(2, versionListing1.getVersionSummaries().size());
    assertEquals(copyObjectResult2.getVersionId(), versionListing1.getVersionSummaries().get(0).getVersionId());
    assertEquals("null", versionListing1.getVersionSummaries().get(1).getVersionId());

    /*-- Copy from a versioning enabled to a versioning disabled bucket. --*/

    // copy without source version ID.
    String text2 = "LocalS3";
    PutObjectResult putObjectResult1 = s3.putObject(bucket2, key2, text2);
    CopyObjectResult copyObjectResult3 = s3.copyObject(bucket2, key2, bucket1, key1);
    assertEquals("null", copyObjectResult3.getVersionId());
    assertTrue(copyObjectResult3.getLastModifiedDate().before(new Date()));
    S3Object object3 = s3.getObject(bucket1, key1);
    assertEquals(text2.length(), object3.getObjectMetadata().getContentLength());
    assertEquals(text2, new String(object3.getObjectContent().readAllBytes()));

    // copy with source version ID.
    CopyObjectResult copyObjectResult4 = s3.copyObject(new CopyObjectRequest()
        .withSourceBucketName(bucket2)
        .withSourceKey(key2)
        .withSourceVersionId(putObjectResult1.getVersionId())
        .withDestinationBucketName(bucket1)
        .withDestinationKey(key1));
    assertEquals("null", copyObjectResult4.getVersionId());
    S3Object object4 = s3.getObject(bucket1, key1);
    assertEquals(text2.length(), object4.getObjectMetadata().getContentLength());
    assertEquals(text2, new String(object4.getObjectContent().readAllBytes()));
  }

  @LocalS3
  @Test
  void testObjectTagging(AmazonS3 s3) {
    String bucketName = "my-bucket";
    String key = "key1";

    s3.createBucket(bucketName);
    s3.putObject(bucketName, key, "Hello");

    assertDoesNotThrow(() -> s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, key)));
    SetObjectTaggingResult setObjectTaggingResult = s3.setObjectTagging(
        new SetObjectTaggingRequest(bucketName, key, new ObjectTagging(List.of(new Tag("K1", "V1"), new Tag("K2", "V2")))));
    assertEquals("null", setObjectTaggingResult.getVersionId());

    GetObjectTaggingResult objectTagging1 = s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, key));
    assertEquals(2, objectTagging1.getTagSet().size());
    Tag tag1 = objectTagging1.getTagSet().get(0);
    Tag tag2 = objectTagging1.getTagSet().get(1);
    assertEquals("K1", tag1.getKey());
    assertEquals("V1", tag1.getValue());
    assertEquals("K2", tag2.getKey());
    assertEquals("V2", tag2.getValue());

    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucketName,
        new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));

    PutObjectResult putObjectResult1 = s3.putObject(bucketName, key, "World");
    assertDoesNotThrow(() -> s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, key)));

    SetObjectTaggingResult setObjectTaggingResult1 = s3.setObjectTagging(
        new SetObjectTaggingRequest(bucketName, key, putObjectResult1.getVersionId(),
            new ObjectTagging(List.of(new Tag("K3", "V3")))));
    assertEquals(putObjectResult1.getVersionId(), setObjectTaggingResult1.getVersionId());

    assertDoesNotThrow(() -> s3.deleteObjectTagging(new DeleteObjectTaggingRequest(bucketName, key)));
  }

  @LocalS3
  @Test
  void testDeleteObjects(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
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
  }

}
