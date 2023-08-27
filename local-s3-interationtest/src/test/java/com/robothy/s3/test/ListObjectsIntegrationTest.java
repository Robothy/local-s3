package com.robothy.s3.test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    assertNull(objectListing.getNextMarker());
    assertNull(objectListing.getDelimiter());
    assertNull(objectListing.getPrefix());
    assertNull(objectListing.getMarker());


    s3.putObject(bucket.getName(), "a.txt", "Hello");
    objectListing = s3.listObjects(bucket.getName());
    assertEquals("my-bucket", objectListing.getBucketName());
    assertEquals(1, objectListing.getObjectSummaries().size());
    assertTrue(objectListing.getCommonPrefixes().isEmpty());
    assertNull(objectListing.getNextMarker());
    assertNull(objectListing.getDelimiter());
    S3ObjectSummary keySummary = objectListing.getObjectSummaries().get(0);
    assertEquals("a.txt", keySummary.getKey());
    assertEquals(DigestUtils.md5Hex("Hello"), keySummary.getETag());
    assertEquals(5, keySummary.getSize());
    assertEquals("STANDARD", keySummary.getStorageClass());
    assertNotNull(keySummary.getLastModified());
    assertNotNull(keySummary.getOwner());


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
    assertTrue(objectListing1.isTruncated());
    // The rest API won't return the next marker if the delimiter is not specified.
    // But the SDK will automatically set the next marker to the last object key if the result is truncated.
    assertEquals("a.txt", objectListing1.getNextMarker());

    listObjectsRequest.withMarker(objectListing1.getObjectSummaries().get(0).getKey())
      .withMaxKeys(2);
    ObjectListing objectListing2 = s3.listObjects(listObjectsRequest);
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals("dir1/a.txt", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/b.txt", objectListing2.getObjectSummaries().get(1).getKey());
    assertEquals(0, objectListing2.getCommonPrefixes().size());
    assertTrue(objectListing2.isTruncated());
    assertEquals("dir1/b.txt", objectListing2.getNextMarker());
  }


  @Test
  @LocalS3
  void listObjectsWithBucketVersioning(AmazonS3 s3) {
    String bucketName = "my-bucket";
    String key = "a.txt";
    s3.createBucket(bucketName);
    s3.putObject(bucketName, key, "Text1");
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucketName,
      new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    s3.putObject(bucketName, key, "Text2");
    ObjectListing objectListing = s3.listObjects(bucketName);
    assertEquals(1, objectListing.getObjectSummaries().size());

    s3.putObject(bucketName, "dir1/a.txt", "Text3");
    s3.putObject(bucketName, "dir1/b.txt", "Text4");

    ObjectListing objectListing1 = s3.listObjects(bucketName);
    assertEquals(3, objectListing1.getObjectSummaries().size());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest(bucketName, null, null, "/", 10));
    assertEquals(1, objectListing2.getObjectSummaries().size());
    assertEquals(1, objectListing2.getCommonPrefixes().size());
    assertEquals(10, objectListing2.getMaxKeys());

    s3.deleteObject(bucketName, key);
    ObjectListing objectListing3 = s3.listObjects(bucketName);
    assertEquals(2, objectListing3.getObjectSummaries().size());
    assertEquals(0, objectListing3.getCommonPrefixes().size());
  }

  @Test
  @LocalS3
  void testListObjectsKeyEncoding(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest(bucketName, null, null, null, null).withEncodingType("url"));
    assertFalse(objectListing.isTruncated());
    assertEquals(1000, objectListing.getMaxKeys());
    assertEquals(4, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());
    assertNull(objectListing.getPrefix());
    assertNull(objectListing.getDelimiter());
    assertNull(objectListing.getMarker());
    assertNull(objectListing.getNextMarker());
    assertEquals("url", objectListing.getEncodingType());
    assertEquals("dir1/key1", objectListing.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectListing.getObjectSummaries().get(1).getKey());
    assertEquals("dir2%40key1", objectListing.getObjectSummaries().get(2).getKey());
    assertEquals("dir2%40key2", objectListing.getObjectSummaries().get(3).getKey());


    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest(bucketName, null, null, null, null)
      .withEncodingType("url").withPrefix("dir2@"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(1000, objectListing1.getMaxKeys());
    assertEquals(2, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertEquals("dir2%40", objectListing1.getPrefix());
    assertNull(objectListing1.getDelimiter());
    assertNull(objectListing1.getMarker());
    assertNull(objectListing1.getNextMarker());
    assertEquals("url", objectListing1.getEncodingType());
    assertEquals("dir2%40key1", objectListing1.getObjectSummaries().get(0).getKey());
    assertEquals("dir2%40key2", objectListing1.getObjectSummaries().get(1).getKey());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withEncodingType("url").withDelimiter("@k"));
    assertFalse(objectListing2.isTruncated());
    assertEquals(1000, objectListing2.getMaxKeys());
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals(1, objectListing2.getCommonPrefixes().size());
    assertNull(objectListing2.getPrefix());
    assertEquals("%40k", objectListing2.getDelimiter());
    assertNull(objectListing2.getMarker());
    assertNull(objectListing2.getNextMarker());
    assertEquals("url", objectListing2.getEncodingType());
    assertEquals("dir1/key1", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectListing2.getObjectSummaries().get(1).getKey());
    assertEquals("dir2%40k", objectListing2.getCommonPrefixes().get(0));


    // invalid encoding type
    assertThrows(AmazonS3Exception.class, () -> s3.listObjects(new ListObjectsRequest(
      bucketName, null, null, "/", 1000).withEncodingType("invalid")));
  }

  @Test
  @LocalS3
  void testListObjectsDelimiter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("/").withMaxKeys(1));
    assertTrue(objectListing.isTruncated());
    assertEquals(1, objectListing.getMaxKeys());
    assertEquals(0, objectListing.getObjectSummaries().size());
    assertEquals(1, objectListing.getCommonPrefixes().size());
    assertNull(objectListing.getPrefix());
    assertEquals("/", objectListing.getDelimiter());
    assertNull(objectListing.getMarker());
    assertEquals("dir1/", objectListing.getNextMarker());
    assertEquals("dir1/", objectListing.getCommonPrefixes().get(0));
  }

  @Test
  @LocalS3
  void testListObjectsDelimiterMarker(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("/").withMaxKeys(1).withMarker("dir1/"));
    assertTrue(objectListing1.isTruncated());
    assertEquals(1, objectListing1.getMaxKeys());
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertNull(objectListing1.getPrefix());
    assertEquals("/", objectListing1.getDelimiter());
    assertEquals("dir1/", objectListing1.getMarker());
    assertEquals("dir2@key1", objectListing1.getNextMarker());
    assertEquals("dir2@key1", objectListing1.getObjectSummaries().get(0).getKey());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("/").withMaxKeys(1).withMarker("dir2@key1"));
    assertFalse(objectListing2.isTruncated());
    assertEquals(1, objectListing2.getMaxKeys());
    assertEquals(1, objectListing2.getObjectSummaries().size());
    assertEquals(0, objectListing2.getCommonPrefixes().size());
    assertNull(objectListing2.getPrefix());
    assertEquals("/", objectListing2.getDelimiter());
    assertEquals("dir2@key1", objectListing2.getMarker());
    assertNull(objectListing2.getNextMarker());
    assertEquals("dir2@key2", objectListing2.getObjectSummaries().get(0).getKey());

    ObjectListing objectListing3 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("@").withMaxKeys(1).withMarker("dir1/key2"));
    assertFalse(objectListing3.isTruncated());
    assertEquals(1, objectListing3.getMaxKeys());
    assertEquals(0, objectListing3.getObjectSummaries().size());
    assertEquals(1, objectListing3.getCommonPrefixes().size());
    assertNull(objectListing3.getPrefix());
    assertEquals("@", objectListing3.getDelimiter());
    assertEquals("dir1/key2", objectListing3.getMarker());
    assertNull(objectListing3.getNextMarker());
    assertEquals("dir2@", objectListing3.getCommonPrefixes().get(0));


  }

  @Test
  @LocalS3
  void testListObjectsDelimiterMaxKeys(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ObjectListing objectListing4 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("k").withMaxKeys(1));
    assertTrue(objectListing4.isTruncated());
    assertEquals(1, objectListing4.getMaxKeys());
    assertEquals(0, objectListing4.getObjectSummaries().size());
    assertEquals(1, objectListing4.getCommonPrefixes().size());
    assertNull(objectListing4.getPrefix());
    assertEquals("k", objectListing4.getDelimiter());
    assertNull(objectListing4.getMarker());
    assertEquals("dir1/k", objectListing4.getNextMarker());
    assertEquals("dir1/k", objectListing4.getCommonPrefixes().get(0));
  }


  @Test
  @LocalS3
  void testListObjectsMarker(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withMarker("dir1/").withMaxKeys(3));
    assertTrue(objectListing.isTruncated());
    assertEquals(3, objectListing.getMaxKeys());
    assertEquals(3, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());
    assertNull(objectListing.getPrefix());
    assertNull(objectListing.getDelimiter());
    assertEquals("dir1/", objectListing.getMarker());
    assertEquals("dir2@key1", objectListing.getNextMarker());
    assertEquals("dir1/key1", objectListing.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectListing.getObjectSummaries().get(1).getKey());
    assertEquals("dir2@key1", objectListing.getObjectSummaries().get(2).getKey());

    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withMarker("dir1/").withMaxKeys(3).withDelimiter("/"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(3, objectListing1.getMaxKeys());
    assertEquals(2, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertNull(objectListing1.getPrefix());
    assertEquals("/", objectListing1.getDelimiter());
    assertEquals("dir1/", objectListing1.getMarker());
    assertNull(objectListing1.getNextMarker());
    assertEquals("dir2@key1", objectListing1.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectListing1.getObjectSummaries().get(1).getKey());
  }

  @Test
  @LocalS3
  void testListObjectsPrefix(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1/"));
    assertFalse(objectListing.isTruncated());
    assertEquals(1000, objectListing.getMaxKeys());
    assertEquals(2, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());
    assertEquals("dir1/", objectListing.getPrefix());
    assertNull(objectListing.getDelimiter());
    assertNull(objectListing.getMarker());
    assertNull(objectListing.getNextMarker());
    assertEquals("dir1/key1", objectListing.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectListing.getObjectSummaries().get(1).getKey());
  }

  @Test
  @LocalS3
  void testListObjectsPrefixDelimiter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    // prefix doesn't contain delimiter
    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("k").withPrefix("dir1"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(1000, objectListing1.getMaxKeys());
    assertEquals(0, objectListing1.getObjectSummaries().size());
    assertEquals(1, objectListing1.getCommonPrefixes().size());
    assertEquals("dir1", objectListing1.getPrefix());
    assertEquals("k", objectListing1.getDelimiter());
    assertNull(objectListing1.getMarker());
    assertNull(objectListing1.getNextMarker());
    assertEquals("dir1/k", objectListing1.getCommonPrefixes().get(0));

    // prefix contains delimiter
    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withDelimiter("d").withPrefix("dir1"));
    assertFalse(objectListing2.isTruncated());
    assertEquals(1000, objectListing2.getMaxKeys());
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals(0, objectListing2.getCommonPrefixes().size());
    assertEquals("dir1", objectListing2.getPrefix());
    assertEquals("d", objectListing2.getDelimiter());
    assertNull(objectListing2.getMarker());
    assertNull(objectListing2.getNextMarker());
    assertEquals("dir1/key1", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectListing2.getObjectSummaries().get(1).getKey());
  }

  @Test
  @LocalS3
  void testListObjectsPrefixMarker(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1/").withMarker("dir1/key2"));
    assertFalse(objectListing.isTruncated());
    assertEquals(1000, objectListing.getMaxKeys());
    assertEquals(0, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());
    assertEquals("dir1/", objectListing.getPrefix());
    assertNull(objectListing.getDelimiter());
    assertEquals("dir1/key2", objectListing.getMarker());
    assertNull(objectListing.getNextMarker());

    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1/").withMarker("dir1/key1"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(1000, objectListing1.getMaxKeys());
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertEquals("dir1/", objectListing1.getPrefix());
    assertNull(objectListing1.getDelimiter());
    assertEquals("dir1/key1", objectListing1.getMarker());
    assertNull(objectListing1.getNextMarker());
    assertEquals("dir1/key2", objectListing1.getObjectSummaries().get(0).getKey());
  }

  @Test
  @LocalS3
  void testListObjectsPrefixMarkerDelimiter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1").withMarker("dir1/key1").withDelimiter("/"));
    assertEquals(0, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());

    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1").withMarker("dir1/key1").withDelimiter("i"));
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertEquals("dir1/key2", objectListing1.getObjectSummaries().get(0).getKey());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir2").withMarker("dir1/key1").withDelimiter("i"));
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals(0, objectListing2.getCommonPrefixes().size());
    assertEquals("dir2@key1", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectListing2.getObjectSummaries().get(1).getKey());

    ObjectListing objectListing3 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir2").withMarker("dir1/key1").withDelimiter("@"));
    assertEquals(0, objectListing3.getObjectSummaries().size());
    assertEquals(1, objectListing3.getCommonPrefixes().size());
    assertEquals("dir2@", objectListing3.getCommonPrefixes().get(0));
  }

  //@Test
  void test() {
    AmazonS3 s3 = AmazonS3Client.builder()
      .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials("", "")))

      .withRegion(Regions.AP_SOUTHEAST_1)
      .build();
    String bucketName = "robothy";

    ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1").withMarker("dir1/key1").withDelimiter("/"));
    assertEquals(0, objectListing.getObjectSummaries().size());
    assertEquals(0, objectListing.getCommonPrefixes().size());

    ObjectListing objectListing1 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir1").withMarker("dir1/key1").withDelimiter("i"));
    assertEquals(1, objectListing1.getObjectSummaries().size());
    assertEquals(0, objectListing1.getCommonPrefixes().size());
    assertEquals("dir1/key2", objectListing1.getObjectSummaries().get(0).getKey());

    ObjectListing objectListing2 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir2").withMarker("dir1/key1").withDelimiter("i"));
    assertEquals(2, objectListing2.getObjectSummaries().size());
    assertEquals(0, objectListing2.getCommonPrefixes().size());
    assertEquals("dir2@key1", objectListing2.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectListing2.getObjectSummaries().get(1).getKey());

    ObjectListing objectListing3 = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName)
      .withPrefix("dir2").withMarker("dir1/key1").withDelimiter("@"));
    assertEquals(0, objectListing3.getObjectSummaries().size());
    assertEquals(1, objectListing3.getCommonPrefixes().size());
    assertEquals("dir2@", objectListing3.getCommonPrefixes().get(0));

    System.out.println("objectsV2Result = %s%n");
  }

  String prepareKeys(AmazonS3 s3, String... keys) {
    String bucket = "test-list-objects" + System.currentTimeMillis();
    s3.createBucket(bucket);
    for (String key : keys) {
      s3.putObject(bucket, key, "Content");
    }
    return bucket;
  }

}
