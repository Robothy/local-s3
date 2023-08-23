package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ListObjectsIntegrationTest {

  @Test
  @LocalS3
  @DisplayName("Test List Objects")
  void testListObjects(AmazonS3 s3) {
    Bucket bucket = s3.createBucket("my-bucket");
    ObjectListing objectListing = s3.listObjects(bucket.getName());
    assertEquals("my-bucket", objectListing.getBucketName());
    assertTrue(objectListing.getObjectSummaries().isEmpty());
    assertTrue(objectListing.getCommonPrefixes().isEmpty());
    assertEquals("", objectListing.getNextMarker());
    assertNull(objectListing.getDelimiter());


    s3.putObject(bucket.getName(), "a.txt", "Hello");
    objectListing = s3.listObjects(bucket.getName());
    assertEquals("my-bucket", objectListing.getBucketName());
    assertEquals(1, objectListing.getObjectSummaries().size());
    assertEquals("a.txt", objectListing.getObjectSummaries().get(0).getKey());
    assertTrue(objectListing.getCommonPrefixes().isEmpty());
    assertEquals("", objectListing.getNextMarker());
    assertNull(objectListing.getDelimiter());


    s3.putObject(bucket.getName(), "dir1/a.txt", "Content A");
    s3.putObject(bucket.getName(), "dir1/b.txt", "Content B");
    s3.putObject(bucket.getName(), "dir2/c.txt", "Content C");
    objectListing = s3.listObjects(bucket.getName(), "dir1/");
    assertEquals("my-bucket", objectListing.getBucketName());
    assertEquals(2, objectListing.getObjectSummaries().size());
    assertEquals("dir1/a.txt", objectListing.getObjectSummaries().get(0).getKey());
    assertEquals(DigestUtils.md5Hex("Content A"), objectListing.getObjectSummaries().get(0).getETag());
    assertEquals("dir1/b.txt", objectListing.getObjectSummaries().get(1).getKey());
    assertEquals(DigestUtils.md5Hex("Content B"), objectListing.getObjectSummaries().get(1).getETag());
    assertEquals(0, objectListing.getCommonPrefixes().size());


    ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
    listObjectsRequest.withBucketName(bucket.getName())
        .withMaxKeys(1);
    ObjectListing objectListing1 = s3.listObjects(listObjectsRequest);
    assertEquals("my-bucket", objectListing1.getBucketName());
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals("a.txt", objectListing1.getObjectSummaries().get(0).getKey());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertEquals("a.txt", objectListing1.getNextMarker());

    listObjectsRequest.withMarker(objectListing1.getNextMarker())
        .withMaxKeys(2);
    ObjectListing objectListing2 = s3.listObjects(listObjectsRequest);
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals("dir1/a.txt", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/b.txt", objectListing2.getObjectSummaries().get(1).getKey());
    assertEquals("dir1/b.txt", objectListing2.getNextMarker());
  }


  @Test
  @LocalS3
  void listObjectsWithDelimiter(AmazonS3 s3) {
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
  void listObjectsWithKeyEncoding(AmazonS3 s3) {
    String bucket = "test-list-objects-with-key-encoding";
    s3.createBucket(bucket);
    s3.putObject(bucket, "my@dir/my@doc.txt", "Text3");

    ObjectListing objectListing1 = s3.listObjects(bucket);
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals("my@dir/my@doc.txt", objectListing1.getObjectSummaries().get(0).getKey());
    assertEquals(0, objectListing1.getCommonPrefixes().size());

    ObjectListing objectListingWithEncodingType = s3.listObjects(new ListObjectsRequest(
        bucket, null, null, null, 1000).withEncodingType("url"));
    assertEquals(1, objectListingWithEncodingType.getObjectSummaries().size());
    assertEquals("my%40dir/my%40doc.txt", objectListingWithEncodingType.getObjectSummaries().get(0).getKey());
    assertEquals(0, objectListingWithEncodingType.getCommonPrefixes().size());

    ObjectListing objectListingWithEncodingTypeAndDelimiter = s3.listObjects(new ListObjectsRequest(
        bucket, null, null, "/", 1000).withEncodingType("url"));
    assertEquals(0, objectListingWithEncodingTypeAndDelimiter.getObjectSummaries().size());
    assertEquals(1, objectListingWithEncodingTypeAndDelimiter.getCommonPrefixes().size());
    assertEquals("my%40dir/", objectListingWithEncodingTypeAndDelimiter.getCommonPrefixes().get(0));


    assertThrows(AmazonS3Exception.class, () -> s3.listObjects(new ListObjectsRequest(
        bucket, null, null, "/", 1000).withEncodingType("invalid")));
  }

}
