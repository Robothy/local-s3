package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.robothy.s3.jupiter.LocalS3;
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


    s3.putObject(bucket.getName(), "a.txt",  "Hello");
    objectListing = s3.listObjects(bucket.getName());
    assertEquals("my-bucket", objectListing.getBucketName());
    assertEquals(1, objectListing.getObjectSummaries().size());
    assertEquals("a.txt", objectListing.getObjectSummaries().get(0).getKey());
    assertTrue(objectListing.getCommonPrefixes().isEmpty());
    assertEquals("", objectListing.getNextMarker());
    assertNull(objectListing.getDelimiter());


    s3.putObject(bucket.getName(), "dir1/a.txt",  "Content A");
    s3.putObject(bucket.getName(), "dir1/b.txt",  "Content B");
    s3.putObject(bucket.getName(), "dir2/c.txt",  "Content C");
    objectListing = s3.listObjects(bucket.getName(), "dir1/");
    assertEquals("my-bucket", objectListing.getBucketName());
    assertEquals(2, objectListing.getObjectSummaries().size());
    assertEquals("dir1/a.txt", objectListing.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/b.txt", objectListing.getObjectSummaries().get(1).getKey());
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

}
