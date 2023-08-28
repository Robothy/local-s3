package com.robothy.s3.test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListObjectsV2IntegrationTest {

  @LocalS3
  @Test
  void testListAllKeys(AmazonS3 s3) {
    String bucket = prepareKeys(s3,
      "dir1/key1",
      "dir1/key2",
      "dir2@key1",
      "dir2@key2");
    ListObjectsV2Result result1 = s3.listObjectsV2(bucket);
    assertEquals(4, result1.getKeyCount());
    assertEquals(0, result1.getCommonPrefixes().size());
    assertFalse(result1.isTruncated());
    assertEquals(bucket, result1.getBucketName());
    assertEquals(4, result1.getKeyCount());
    assertNull(result1.getNextContinuationToken());
    assertNull(result1.getPrefix());
    assertNull(result1.getDelimiter());
    assertEquals(1000, result1.getMaxKeys());
    assertNull(result1.getEncodingType());
    assertNull(result1.getContinuationToken());
    assertNull(result1.getStartAfter());

    S3ObjectSummary dir1key1 = result1.getObjectSummaries().get(0);
    assertEquals("dir1/key1", dir1key1.getKey());
    assertNotNull(DigestUtils.md5Hex("Content"), dir1key1.getETag());
    assertEquals(7, dir1key1.getSize());
    assertNotNull(dir1key1.getLastModified());
    assertNotNull(dir1key1.getStorageClass());
    assertNull(dir1key1.getOwner());
    assertEquals("STANDARD", dir1key1.getStorageClass());
  }

  @Test
  @LocalS3
  void testListObjectsV2EncodingType(AmazonS3 s3) {
    String bucket = prepareKeys(s3,
      "dir1/key1",
      "dir1/key2",
      "dir2@key1",
      "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucket).withEncodingType("url"));
    assertEquals(4, objectsV2Result.getKeyCount());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertFalse(objectsV2Result.isTruncated());
    assertEquals(bucket, objectsV2Result.getBucketName());
    assertEquals(4, objectsV2Result.getKeyCount());
    assertNull(objectsV2Result.getNextContinuationToken());
    assertNull(objectsV2Result.getPrefix());
    assertNull(objectsV2Result.getDelimiter());
    assertEquals(1000, objectsV2Result.getMaxKeys());
    assertEquals("url", objectsV2Result.getEncodingType());
    assertNull(objectsV2Result.getContinuationToken());
    assertNull(objectsV2Result.getStartAfter());

    // the SDK will decode the key automatically.
    assertEquals("dir1/key1", objectsV2Result.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectsV2Result.getObjectSummaries().get(1).getKey());
    assertEquals("dir2@key1", objectsV2Result.getObjectSummaries().get(2).getKey());
    assertEquals("dir2@key2", objectsV2Result.getObjectSummaries().get(3).getKey());
  }

  @Test
  @LocalS3
  void testListObjectsV2WithDelimiter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withDelimiter("/"));
    assertEquals(2, objectsV2Result.getObjectSummaries().size());
    assertEquals(1, objectsV2Result.getCommonPrefixes().size());
    assertFalse(objectsV2Result.isTruncated());
    assertEquals(bucketName, objectsV2Result.getBucketName());
    assertEquals(3, objectsV2Result.getKeyCount());
    assertNull(objectsV2Result.getNextContinuationToken());
    assertNull(objectsV2Result.getPrefix());
    assertEquals("/", objectsV2Result.getDelimiter());
    assertEquals(1000, objectsV2Result.getMaxKeys());
    assertNull(objectsV2Result.getEncodingType());
    assertNull(objectsV2Result.getContinuationToken());
    assertNull(objectsV2Result.getStartAfter());

    assertEquals("dir1/", objectsV2Result.getCommonPrefixes().get(0));
    assertEquals("dir2@key1", objectsV2Result.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectsV2Result.getObjectSummaries().get(1).getKey());


    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withDelimiter("k"));
    assertEquals("k", objectsV2Result1.getDelimiter());
    assertEquals(0, objectsV2Result1.getObjectSummaries().size());
    assertEquals(2, objectsV2Result1.getCommonPrefixes().size());
    assertEquals("dir1/k", objectsV2Result1.getCommonPrefixes().get(0));
    assertEquals("dir2@k", objectsV2Result1.getCommonPrefixes().get(1));


    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withDelimiter("dir"));
    assertEquals("dir", objectsV2Result2.getDelimiter());
    assertEquals(0, objectsV2Result2.getObjectSummaries().size());
    assertEquals(1, objectsV2Result2.getCommonPrefixes().size());
    assertEquals("dir", objectsV2Result2.getCommonPrefixes().get(0));

    ListObjectsV2Result objectsV2Result3 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withDelimiter("key2"));
    assertEquals("key2", objectsV2Result3.getDelimiter());
    assertEquals(2, objectsV2Result3.getObjectSummaries().size());
    assertEquals(2, objectsV2Result3.getCommonPrefixes().size());
    assertEquals("dir1/key1", objectsV2Result3.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key1", objectsV2Result3.getObjectSummaries().get(1).getKey());
    assertEquals("dir1/key2", objectsV2Result3.getCommonPrefixes().get(0));
    assertEquals("dir2@key2", objectsV2Result3.getCommonPrefixes().get(1));
    assertFalse(objectsV2Result3.isTruncated());

    ListObjectsV2Result objectsV2Result4 = s3.listObjectsV2(new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withDelimiter("key2")
      .withMaxKeys(2));
    assertEquals("key2", objectsV2Result4.getDelimiter());
    assertEquals(1, objectsV2Result4.getObjectSummaries().size());
    assertEquals(1, objectsV2Result4.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result4.getKeyCount());
    assertNotNull(objectsV2Result4.getNextContinuationToken());
    assertEquals("dir1/key1", objectsV2Result4.getObjectSummaries().get(0).getKey());
    assertEquals("dir1/key2", objectsV2Result4.getCommonPrefixes().get(0));
    assertTrue(objectsV2Result4.isTruncated());
  }

  @Test
  @LocalS3
  void testListObjectsV2StartAfter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withStartAfter("dir1"));
    assertFalse(objectsV2Result.isTruncated());
    assertEquals(4, objectsV2Result.getObjectSummaries().size());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertEquals(4, objectsV2Result.getKeyCount());
    assertEquals("dir1", objectsV2Result.getStartAfter());

    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withStartAfter("dir1/key1").withMaxKeys(2));
    assertTrue(objectsV2Result1.isTruncated());
    assertEquals(2, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result1.getKeyCount());
    assertEquals("dir1/key1", objectsV2Result1.getStartAfter());
    assertNotNull(objectsV2Result1.getNextContinuationToken());
    assertEquals("dir1/key2", objectsV2Result1.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key1", objectsV2Result1.getObjectSummaries().get(1).getKey());

    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withStartAfter("dir2@key2"));
    assertFalse(objectsV2Result2.isTruncated());
    assertEquals(0, objectsV2Result2.getObjectSummaries().size());
    assertEquals(0, objectsV2Result2.getCommonPrefixes().size());
    assertEquals(0, objectsV2Result2.getKeyCount());
    assertEquals("dir2@key2", objectsV2Result2.getStartAfter());
    assertNull(objectsV2Result2.getNextContinuationToken());
  }


  @LocalS3
  @Test
  void testListObjectsV2Prefix(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withPrefix("dir1"));
    assertFalse(objectsV2Result.isTruncated());
    assertEquals(2, objectsV2Result.getObjectSummaries().size());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result.getKeyCount());
    assertEquals("dir1", objectsV2Result.getPrefix());
    assertEquals(1000, objectsV2Result.getMaxKeys());
    assertNull(objectsV2Result.getNextContinuationToken());

    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withPrefix("dir")
      .withMaxKeys(2));
    assertTrue(objectsV2Result1.isTruncated());
    assertEquals(2, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result1.getKeyCount());
    assertEquals("dir", objectsV2Result1.getPrefix());
    assertEquals(2, objectsV2Result1.getMaxKeys());
    assertNotNull(objectsV2Result1.getNextContinuationToken());

    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withContinuationToken(objectsV2Result1.getNextContinuationToken()));
    assertFalse(objectsV2Result2.isTruncated());
    assertEquals(2, objectsV2Result2.getObjectSummaries().size());
    assertEquals(0, objectsV2Result2.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result2.getKeyCount());
    assertNull(objectsV2Result2.getPrefix());
    assertEquals(1000, objectsV2Result2.getMaxKeys());
    assertNull(objectsV2Result2.getStartAfter());
    assertNull(objectsV2Result2.getNextContinuationToken());
    assertEquals("dir2@key1", objectsV2Result2.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectsV2Result2.getObjectSummaries().get(1).getKey());

    ListObjectsV2Result objectsV2Result3 = s3.listObjectsV2(new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withPrefix("dir")
      .withStartAfter("dir1/key2")
      .withMaxKeys(2));
    assertFalse(objectsV2Result3.isTruncated());
    assertEquals(2, objectsV2Result3.getObjectSummaries().size());
    assertEquals(0, objectsV2Result3.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result3.getKeyCount());
    assertEquals("dir", objectsV2Result3.getPrefix());
    assertEquals(2, objectsV2Result3.getMaxKeys());
    assertEquals("dir1/key2", objectsV2Result3.getStartAfter());
    assertNull(objectsV2Result3.getNextContinuationToken());
    assertEquals("dir2@key1", objectsV2Result3.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectsV2Result3.getObjectSummaries().get(1).getKey());
  }

  @Test
  @LocalS3
  void testFetchOwner(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "key1", "key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName));
    assertEquals(2, objectsV2Result.getObjectSummaries().size());
    assertNull(objectsV2Result.getObjectSummaries().get(0).getOwner());
    assertNull(objectsV2Result.getObjectSummaries().get(1).getOwner());

    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withFetchOwner(true));
    assertEquals(2, objectsV2Result1.getObjectSummaries().size());
    Owner owner1 = objectsV2Result1.getObjectSummaries().get(0).getOwner();
    assertNotNull(owner1);
    assertNotNull(owner1.getDisplayName());
    assertNotNull(owner1.getId());
    Owner owner2 = objectsV2Result1.getObjectSummaries().get(1).getOwner();
    assertNotNull(owner2);
    assertNotNull(owner2.getDisplayName());
    assertNotNull(owner2.getId());

    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withFetchOwner(false));
    assertEquals(2, objectsV2Result2.getObjectSummaries().size());
    assertNull(objectsV2Result2.getObjectSummaries().get(0).getOwner());
    assertNull(objectsV2Result2.getObjectSummaries().get(1).getOwner());
  }

  @Test
  @LocalS3
  void testListObjectsV2WithContinuationToken(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2));
    assertTrue(objectsV2Result.isTruncated());
    assertEquals(2, objectsV2Result.getObjectSummaries().size());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result.getKeyCount());
    assertEquals(2, objectsV2Result.getMaxKeys());
    assertNotNull(objectsV2Result.getNextContinuationToken());
    assertNull(objectsV2Result.getPrefix());
    assertNull(objectsV2Result.getDelimiter());
    assertNull(objectsV2Result.getEncodingType());
    assertNull(objectsV2Result.getContinuationToken());
    assertNull(objectsV2Result.getStartAfter());

    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result.getNextContinuationToken()));
    assertFalse(objectsV2Result1.isTruncated());
    assertEquals(2, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result1.getKeyCount());
    assertEquals(1000, objectsV2Result1.getMaxKeys());
    assertNull(objectsV2Result1.getNextContinuationToken());
    assertNull(objectsV2Result1.getPrefix());
    assertNull(objectsV2Result1.getDelimiter());
    assertNull(objectsV2Result1.getEncodingType());
    assertEquals(objectsV2Result.getNextContinuationToken(), objectsV2Result1.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir2@key1", objectsV2Result1.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectsV2Result1.getObjectSummaries().get(1).getKey());

    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result.getNextContinuationToken())
      .withMaxKeys(1));
    assertTrue(objectsV2Result2.isTruncated());
    assertEquals(1, objectsV2Result2.getObjectSummaries().size());
    assertEquals(0, objectsV2Result2.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result2.getKeyCount());
    assertEquals(1, objectsV2Result2.getMaxKeys());
    assertNotNull(objectsV2Result2.getNextContinuationToken());
    assertNull(objectsV2Result2.getPrefix());
    assertNull(objectsV2Result2.getDelimiter());
    assertNull(objectsV2Result2.getEncodingType());
    assertEquals(objectsV2Result.getNextContinuationToken(), objectsV2Result2.getContinuationToken());
    assertNull(objectsV2Result2.getStartAfter());
    assertEquals("dir2@key1", objectsV2Result2.getObjectSummaries().get(0).getKey());

    ListObjectsV2Result objectsV2Result3 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result2.getNextContinuationToken()));
    assertFalse(objectsV2Result3.isTruncated());
    assertEquals(1, objectsV2Result3.getObjectSummaries().size());
    assertEquals(0, objectsV2Result3.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result3.getKeyCount());
    assertEquals(1000, objectsV2Result3.getMaxKeys());
    assertNull(objectsV2Result3.getNextContinuationToken());
    assertNull(objectsV2Result3.getPrefix());
    assertNull(objectsV2Result3.getDelimiter());
    assertNull(objectsV2Result3.getEncodingType());
    assertEquals(objectsV2Result2.getNextContinuationToken(), objectsV2Result3.getContinuationToken());
    assertNull(objectsV2Result3.getStartAfter());
    assertEquals("dir2@key2", objectsV2Result3.getObjectSummaries().get(0).getKey());
  }

  @Test
  @LocalS3
  void testContinuationTokenWithDelimiter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withDelimiter("k")
      .withMaxKeys(1));
    assertTrue(objectsV2Result.isTruncated());
    assertEquals(0, objectsV2Result.getObjectSummaries().size());
    assertEquals(1, objectsV2Result.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result.getKeyCount());
    assertEquals(1, objectsV2Result.getMaxKeys());
    assertNotNull(objectsV2Result.getNextContinuationToken());
    assertEquals("k", objectsV2Result.getDelimiter());

    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result.getNextContinuationToken()));
    assertFalse(objectsV2Result1.isTruncated());
    assertEquals(2, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(2, objectsV2Result1.getKeyCount());
    assertEquals(1000, objectsV2Result1.getMaxKeys());
    assertNull(objectsV2Result1.getNextContinuationToken());
    assertNull(objectsV2Result1.getPrefix());
    assertNull(objectsV2Result1.getDelimiter());
    assertNull(objectsV2Result1.getEncodingType());
    assertEquals(objectsV2Result.getNextContinuationToken(), objectsV2Result1.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir2@key1", objectsV2Result1.getObjectSummaries().get(0).getKey());
    assertEquals("dir2@key2", objectsV2Result1.getObjectSummaries().get(1).getKey());
  }

  @Test
  @LocalS3
  void testContinuationTokenWithStartAfter(AmazonS3 s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withMaxKeys(1));
    assertTrue(objectsV2Result.isTruncated());
    assertEquals(1, objectsV2Result.getObjectSummaries().size());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result.getKeyCount());
    assertEquals(1, objectsV2Result.getMaxKeys());
    assertNotNull(objectsV2Result.getNextContinuationToken());
    assertNull(objectsV2Result.getPrefix());
    assertNull(objectsV2Result.getDelimiter());
    assertNull(objectsV2Result.getEncodingType());
    assertNull(objectsV2Result.getContinuationToken());
    assertNull(objectsV2Result.getStartAfter());
    assertEquals("dir1/key1", objectsV2Result.getObjectSummaries().get(0).getKey());

    // now, the continuation token is after "dir1/key1"
    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result.getNextContinuationToken())
      .withStartAfter("dir1/key2")
      .withMaxKeys(1));
    assertTrue(objectsV2Result1.isTruncated());
    assertEquals(1, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result1.getKeyCount());
    assertEquals(1, objectsV2Result1.getMaxKeys());
    assertNotNull(objectsV2Result1.getNextContinuationToken());
    assertNull(objectsV2Result1.getPrefix());
    assertNull(objectsV2Result1.getDelimiter());
    assertNull(objectsV2Result1.getEncodingType());
    assertEquals(objectsV2Result.getNextContinuationToken(), objectsV2Result1.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir1/key2", objectsV2Result1.getObjectSummaries().get(0).getKey());

    // now, the continuation token is after "dir1@key2"
    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
      .withContinuationToken(objectsV2Result1.getNextContinuationToken())
      .withStartAfter("dir1/key1")
      .withMaxKeys(1));
    assertTrue(objectsV2Result2.isTruncated());
    assertEquals(1, objectsV2Result2.getObjectSummaries().size());
    assertEquals(0, objectsV2Result2.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result2.getKeyCount());
    assertEquals(1, objectsV2Result2.getMaxKeys());
    assertNotNull(objectsV2Result2.getNextContinuationToken());
    assertNull(objectsV2Result2.getPrefix());
    assertNull(objectsV2Result2.getDelimiter());
    assertNull(objectsV2Result2.getEncodingType());
    assertEquals(objectsV2Result1.getNextContinuationToken(), objectsV2Result2.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir2@key1", objectsV2Result2.getObjectSummaries().get(0).getKey());
  }

  //@Test
  void test() {
    AmazonS3 s3 = AmazonS3Client.builder()
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials("AKIA372Q46RLSTQAB7HY", "fvf/ACjJA2rzS1iFHfByNV1V/ZuXlezaI97ZLGV0")))

        .withRegion(Regions.AP_SOUTHEAST_1)
        .build();
    String bucketName = "robothy";

    ListObjectsV2Result objectsV2Result = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
        .withMaxKeys(1));
    assertTrue(objectsV2Result.isTruncated());
    assertEquals(1, objectsV2Result.getObjectSummaries().size());
    assertEquals(0, objectsV2Result.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result.getKeyCount());
    assertEquals(1, objectsV2Result.getMaxKeys());
    assertNotNull(objectsV2Result.getNextContinuationToken());
    assertNull(objectsV2Result.getPrefix());
    assertNull(objectsV2Result.getDelimiter());
    assertNull(objectsV2Result.getEncodingType());
    assertNull(objectsV2Result.getContinuationToken());
    assertNull(objectsV2Result.getStartAfter());
    assertEquals("dir1/key1", objectsV2Result.getObjectSummaries().get(0).getKey());

    // now, the continuation token is after "dir1/key1"
    ListObjectsV2Result objectsV2Result1 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
        .withContinuationToken(objectsV2Result.getNextContinuationToken())
        .withStartAfter("dir1/key2")
        .withMaxKeys(1));
    assertTrue(objectsV2Result1.isTruncated());
    assertEquals(1, objectsV2Result1.getObjectSummaries().size());
    assertEquals(0, objectsV2Result1.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result1.getKeyCount());
    assertEquals(1, objectsV2Result1.getMaxKeys());
    assertNotNull(objectsV2Result1.getNextContinuationToken());
    assertNull(objectsV2Result1.getPrefix());
    assertNull(objectsV2Result1.getDelimiter());
    assertNull(objectsV2Result1.getEncodingType());
    assertEquals(objectsV2Result.getNextContinuationToken(), objectsV2Result1.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir1/key2", objectsV2Result1.getObjectSummaries().get(0).getKey());

    // now, the continuation token is after "dir1@key2"
    ListObjectsV2Result objectsV2Result2 = s3.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName)
        .withContinuationToken(objectsV2Result1.getNextContinuationToken())
        .withStartAfter("dir1/key1")
        .withMaxKeys(1));
    assertTrue(objectsV2Result2.isTruncated());
    assertEquals(1, objectsV2Result2.getObjectSummaries().size());
    assertEquals(0, objectsV2Result2.getCommonPrefixes().size());
    assertEquals(1, objectsV2Result2.getKeyCount());
    assertEquals(1, objectsV2Result2.getMaxKeys());
    assertNotNull(objectsV2Result2.getNextContinuationToken());
    assertNull(objectsV2Result2.getPrefix());
    assertNull(objectsV2Result2.getDelimiter());
    assertNull(objectsV2Result2.getEncodingType());
    assertEquals(objectsV2Result1.getNextContinuationToken(), objectsV2Result2.getContinuationToken());
    assertNull(objectsV2Result1.getStartAfter());
    assertEquals("dir2@key1", objectsV2Result2.getObjectSummaries().get(0).getKey());

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
