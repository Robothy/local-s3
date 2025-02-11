package com.robothy.s3.test;

import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.EncodingType;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.ObjectStorageClass;

import static org.junit.jupiter.api.Assertions.*;

public class ListObjectsV2IntegrationTest {

  @LocalS3
  @Test
  void testListAllKeys(S3Client s3) {
    String bucket = prepareKeys(s3,
      "dir1/key1",
      "dir1/key2",
      "dir2@key1",
      "dir2@key2");
    ListObjectsV2Response result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucket)
      .build());
    assertEquals(4, result1.contents().size());
    assertEquals(0, result1.commonPrefixes().size());
    assertNotEquals(Boolean.TRUE, result1.isTruncated());
    assertEquals(bucket, result1.name());
    assertNull(result1.nextContinuationToken());
    assertNull(result1.prefix());
    assertNull(result1.delimiter());
    assertEquals(1000, result1.maxKeys());
    assertNull(result1.encodingType());
    assertNull(result1.continuationToken());
    assertNull(result1.startAfter());

    assertEquals("dir1/key1", result1.contents().get(0).key());
    assertNotNull(DigestUtils.md5Hex("Content"), result1.contents().get(0).eTag());
    assertEquals(7, result1.contents().get(0).size());
    assertNotNull(result1.contents().get(0).lastModified());
    assertNotNull(result1.contents().get(0).storageClass());
    assertEquals(ObjectStorageClass.STANDARD, result1.contents().get(0).storageClass());
  }

  @Test
  @LocalS3
  void testListObjectsV2EncodingType(S3Client s3) {
    String bucket = prepareKeys(s3,
      "dir1/key1",
      "dir1/key2",
      "dir2@key1",
      "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucket)
      .encodingType("url")
      .build());
    assertEquals(4, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertFalse(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(bucket, objectsV2Result.name());
    assertNull(objectsV2Result.nextContinuationToken());
    assertNull(objectsV2Result.prefix());
    assertNull(objectsV2Result.delimiter());
    assertEquals(1000, objectsV2Result.maxKeys());
    assertEquals(EncodingType.URL, objectsV2Result.encodingType());
    assertNull(objectsV2Result.continuationToken());
    assertNull(objectsV2Result.startAfter());

    // the SDK will decode the key automatically.
    assertEquals("dir1/key1", objectsV2Result.contents().get(0).key());
    assertEquals("dir1/key2", objectsV2Result.contents().get(1).key());
    assertEquals("dir2@key1", objectsV2Result.contents().get(2).key());
    assertEquals("dir2@key2", objectsV2Result.contents().get(3).key());
  }

  @Test
  @LocalS3
  void testListObjectsV2WithDelimiter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("/")
      .build());
    assertEquals(2, objectsV2Result.contents().size());
    assertEquals(1, objectsV2Result.commonPrefixes().size());
    assertFalse(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(bucketName, objectsV2Result.name());
    assertEquals(3, objectsV2Result.keyCount());
    assertNull(objectsV2Result.nextContinuationToken());
    assertNull(objectsV2Result.prefix());
    assertEquals("/", objectsV2Result.delimiter());
    assertEquals(1000, objectsV2Result.maxKeys());
    assertNull(objectsV2Result.encodingType());
    assertNull(objectsV2Result.continuationToken());
    assertNull(objectsV2Result.startAfter());

    assertEquals("dir1/", objectsV2Result.commonPrefixes().get(0).prefix());
    assertEquals("dir2@key1", objectsV2Result.contents().get(0).key());
    assertEquals("dir2@key2", objectsV2Result.contents().get(1).key());


    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("k")
      .build());
    assertEquals("k", objectsV2Result1.delimiter());
    assertEquals(0, objectsV2Result1.contents().size());
    assertEquals(2, objectsV2Result1.commonPrefixes().size());
    assertEquals("dir1/k", objectsV2Result1.commonPrefixes().get(0).prefix());
    assertEquals("dir2@k", objectsV2Result1.commonPrefixes().get(1).prefix());


    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("dir")
      .build());
    assertEquals("dir", objectsV2Result2.delimiter());
    assertEquals(0, objectsV2Result2.contents().size());
    assertEquals(1, objectsV2Result2.commonPrefixes().size());
    assertEquals("dir", objectsV2Result2.commonPrefixes().get(0).prefix());

    ListObjectsV2Response objectsV2Result3 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("key2")
      .build());
    assertEquals("key2", objectsV2Result3.delimiter());
    assertEquals(2, objectsV2Result3.contents().size());
    assertEquals(2, objectsV2Result3.commonPrefixes().size());
    assertEquals("dir1/key1", objectsV2Result3.contents().get(0).key());
    assertEquals("dir2@key1", objectsV2Result3.contents().get(1).key());
    assertEquals("dir1/key2", objectsV2Result3.commonPrefixes().get(0).prefix());
    assertEquals("dir2@key2", objectsV2Result3.commonPrefixes().get(1).prefix());
    assertNotEquals(Boolean.TRUE, objectsV2Result3.isTruncated());

    ListObjectsV2Response objectsV2Result4 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("key2")
      .maxKeys(2)
      .build());
    assertEquals("key2", objectsV2Result4.delimiter());
    assertEquals(1, objectsV2Result4.contents().size());
    assertEquals(1, objectsV2Result4.commonPrefixes().size());
    assertEquals(2, objectsV2Result4.keyCount());
    assertNotNull(objectsV2Result4.nextContinuationToken());
    assertEquals("dir1/key1", objectsV2Result4.contents().get(0).key());
    assertEquals("dir1/key2", objectsV2Result4.commonPrefixes().get(0).prefix());
    assertEquals(Boolean.TRUE, objectsV2Result4.isTruncated());
  }

  @Test
  @LocalS3
  void testListObjectsV2StartAfter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .startAfter("dir1")
      .build());
    assertNotEquals(Boolean.TRUE, objectsV2Result.isTruncated());
    assertEquals(4, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertEquals(4, objectsV2Result.keyCount());
    assertEquals("dir1", objectsV2Result.startAfter());

    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .startAfter("dir1/key1")
      .maxKeys(2)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(2, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(2, objectsV2Result1.keyCount());
    assertEquals("dir1/key1", objectsV2Result1.startAfter());
    assertNotNull(objectsV2Result1.nextContinuationToken());
    assertEquals("dir1/key2", objectsV2Result1.contents().get(0).key());
    assertEquals("dir2@key1", objectsV2Result1.contents().get(1).key());

    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .startAfter("dir2@key2")
      .build());
    assertNotEquals(Boolean.TRUE, objectsV2Result2.isTruncated());
    assertEquals(0, objectsV2Result2.contents().size());
    assertEquals(0, objectsV2Result2.commonPrefixes().size());
    assertEquals(0, objectsV2Result2.keyCount());
    assertEquals("dir2@key2", objectsV2Result2.startAfter());
    assertNull(objectsV2Result2.nextContinuationToken());
  }


  @LocalS3
  @Test
  void testListObjectsV2Prefix(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .prefix("dir1")
      .build());
    assertNotEquals(Boolean.TRUE, objectsV2Result.isTruncated());
    assertEquals(2, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertEquals(2, objectsV2Result.keyCount());
    assertEquals("dir1", objectsV2Result.prefix());
    assertEquals(1000, objectsV2Result.maxKeys());
    assertNull(objectsV2Result.nextContinuationToken());

    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .prefix("dir")
      .maxKeys(2)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(2, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(2, objectsV2Result1.keyCount());
    assertEquals("dir", objectsV2Result1.prefix());
    assertEquals(2, objectsV2Result1.maxKeys());
    assertNotNull(objectsV2Result1.nextContinuationToken());

    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result1.nextContinuationToken())
      .build());
    assertFalse(Boolean.TRUE.equals(objectsV2Result2.isTruncated()));
    assertEquals(2, objectsV2Result2.contents().size());
    assertEquals(0, objectsV2Result2.commonPrefixes().size());
    assertEquals(2, objectsV2Result2.keyCount());
    assertNull(objectsV2Result2.prefix());
    assertEquals(1000, objectsV2Result2.maxKeys());
    assertNull(objectsV2Result2.startAfter());
    assertNull(objectsV2Result2.nextContinuationToken());
    assertEquals("dir2@key1", objectsV2Result2.contents().get(0).key());
    assertEquals("dir2@key2", objectsV2Result2.contents().get(1).key());

    ListObjectsV2Response objectsV2Result3 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .prefix("dir")
      .startAfter("dir1/key2")
      .maxKeys(2)
      .build());
    assertFalse(Boolean.TRUE.equals(objectsV2Result3.isTruncated()));
    assertEquals(2, objectsV2Result3.contents().size());
    assertEquals(0, objectsV2Result3.commonPrefixes().size());
    assertEquals(2, objectsV2Result3.keyCount());
    assertEquals("dir", objectsV2Result3.prefix());
    assertEquals(2, objectsV2Result3.maxKeys());
    assertEquals("dir1/key2", objectsV2Result3.startAfter());
    assertNull(objectsV2Result3.nextContinuationToken());
    assertEquals("dir2@key1", objectsV2Result3.contents().get(0).key());
    assertEquals("dir2@key2", objectsV2Result3.contents().get(1).key());
  }

  @Test
  @LocalS3
  void testFetchOwner(S3Client s3) {
    String bucketName = prepareKeys(s3, "key1", "key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .build());
    assertEquals(2, objectsV2Result.contents().size());

    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .fetchOwner(true)
      .build());
    assertEquals(2, objectsV2Result1.contents().size());

    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .fetchOwner(false)
      .build());
    assertEquals(2, objectsV2Result2.contents().size());
  }

  @Test
  @LocalS3
  void testListObjectsV2WithContinuationToken(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .maxKeys(2)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(2, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertEquals(2, objectsV2Result.keyCount());
    assertEquals(2, objectsV2Result.maxKeys());
    assertNotNull(objectsV2Result.nextContinuationToken());
    assertNull(objectsV2Result.prefix());
    assertNull(objectsV2Result.delimiter());
    assertNull(objectsV2Result.encodingType());
    assertNull(objectsV2Result.continuationToken());
    assertNull(objectsV2Result.startAfter());

    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result.nextContinuationToken())
      .build());
    assertFalse(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(2, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(2, objectsV2Result1.keyCount());
    assertEquals(1000, objectsV2Result1.maxKeys());
    assertNull(objectsV2Result1.nextContinuationToken());
    assertNull(objectsV2Result1.prefix());
    assertNull(objectsV2Result1.delimiter());
    assertNull(objectsV2Result1.encodingType());
    assertEquals(objectsV2Result.nextContinuationToken(), objectsV2Result1.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir2@key1", objectsV2Result1.contents().get(0).key());
    assertEquals("dir2@key2", objectsV2Result1.contents().get(1).key());

    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result.nextContinuationToken())
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result2.isTruncated()));
    assertEquals(1, objectsV2Result2.contents().size());
    assertEquals(0, objectsV2Result2.commonPrefixes().size());
    assertEquals(1, objectsV2Result2.keyCount());
    assertEquals(1, objectsV2Result2.maxKeys());
    assertNotNull(objectsV2Result2.nextContinuationToken());
    assertNull(objectsV2Result2.prefix());
    assertNull(objectsV2Result2.delimiter());
    assertNull(objectsV2Result2.encodingType());
    assertEquals(objectsV2Result.nextContinuationToken(), objectsV2Result2.continuationToken());
    assertNull(objectsV2Result2.startAfter());
    assertEquals("dir2@key1", objectsV2Result2.contents().get(0).key());

    ListObjectsV2Response objectsV2Result3 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result2.nextContinuationToken())
      .build());
    assertFalse(Boolean.TRUE.equals(objectsV2Result3.isTruncated()));
    assertEquals(1, objectsV2Result3.contents().size());
    assertEquals(0, objectsV2Result3.commonPrefixes().size());
    assertEquals(1, objectsV2Result3.keyCount());
    assertEquals(1000, objectsV2Result3.maxKeys());
    assertNull(objectsV2Result3.nextContinuationToken());
    assertNull(objectsV2Result3.prefix());
    assertNull(objectsV2Result3.delimiter());
    assertNull(objectsV2Result3.encodingType());
    assertEquals(objectsV2Result2.nextContinuationToken(), objectsV2Result3.continuationToken());
    assertNull(objectsV2Result3.startAfter());
    assertEquals("dir2@key2", objectsV2Result3.contents().get(0).key());
  }

  @Test
  @LocalS3
  void testContinuationTokenWithDelimiter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .delimiter("k")
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(0, objectsV2Result.contents().size());
    assertEquals(1, objectsV2Result.commonPrefixes().size());
    assertEquals(1, objectsV2Result.keyCount());
    assertEquals(1, objectsV2Result.maxKeys());
    assertNotNull(objectsV2Result.nextContinuationToken());
    assertEquals("k", objectsV2Result.delimiter());

    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result.nextContinuationToken())
      .build());
    assertFalse(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(2, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(2, objectsV2Result1.keyCount());
    assertEquals(1000, objectsV2Result1.maxKeys());
    assertNull(objectsV2Result1.nextContinuationToken());
    assertNull(objectsV2Result1.prefix());
    assertNull(objectsV2Result1.delimiter());
    assertNull(objectsV2Result1.encodingType());
    assertEquals(objectsV2Result.nextContinuationToken(), objectsV2Result1.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir2@key1", objectsV2Result1.contents().get(0).key());
    assertEquals("dir2@key2", objectsV2Result1.contents().get(1).key());
  }

  @Test
  @LocalS3
  void testContinuationTokenWithStartAfter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(1, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertEquals(1, objectsV2Result.keyCount());
    assertEquals(1, objectsV2Result.maxKeys());
    assertNotNull(objectsV2Result.nextContinuationToken());
    assertNull(objectsV2Result.prefix());
    assertNull(objectsV2Result.delimiter());
    assertNull(objectsV2Result.encodingType());
    assertNull(objectsV2Result.continuationToken());
    assertNull(objectsV2Result.startAfter());
    assertEquals("dir1/key1", objectsV2Result.contents().get(0).key());

    // now, the continuation token is after "dir1/key1"
    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result.nextContinuationToken())
      .startAfter("dir1/key2")
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(1, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(1, objectsV2Result1.keyCount());
    assertEquals(1, objectsV2Result1.maxKeys());
    assertNotNull(objectsV2Result1.nextContinuationToken());
    assertNull(objectsV2Result1.prefix());
    assertNull(objectsV2Result1.delimiter());
    assertNull(objectsV2Result1.encodingType());
    assertEquals(objectsV2Result.nextContinuationToken(), objectsV2Result1.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir1/key2", objectsV2Result1.contents().get(0).key());

    // now, the continuation token is after "dir1@key2"
    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result1.nextContinuationToken())
      .startAfter("dir1/key1")
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result2.isTruncated()));
    assertEquals(1, objectsV2Result2.contents().size());
    assertEquals(0, objectsV2Result2.commonPrefixes().size());
    assertEquals(1, objectsV2Result2.keyCount());
    assertEquals(1, objectsV2Result2.maxKeys());
    assertNotNull(objectsV2Result2.nextContinuationToken());
    assertNull(objectsV2Result2.prefix());
    assertNull(objectsV2Result2.delimiter());
    assertNull(objectsV2Result2.encodingType());
    assertEquals(objectsV2Result1.nextContinuationToken(), objectsV2Result2.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir2@key1", objectsV2Result2.contents().get(0).key());
  }

  //@Test
  void test() {
    S3Client s3 = S3Client.builder()
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("", "")))
      .region(Region.AP_SOUTHEAST_1)
      .build();
    String bucketName = "robothy";

    ListObjectsV2Response objectsV2Result = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result.isTruncated()));
    assertEquals(1, objectsV2Result.contents().size());
    assertEquals(0, objectsV2Result.commonPrefixes().size());
    assertEquals(1, objectsV2Result.keyCount());
    assertEquals(1, objectsV2Result.maxKeys());
    assertNotNull(objectsV2Result.nextContinuationToken());
    assertNull(objectsV2Result.prefix());
    assertNull(objectsV2Result.delimiter());
    assertNull(objectsV2Result.encodingType());
    assertNull(objectsV2Result.continuationToken());
    assertNull(objectsV2Result.startAfter());
    assertEquals("dir1/key1", objectsV2Result.contents().get(0).key());

    // now, the continuation token is after "dir1/key1"
    ListObjectsV2Response objectsV2Result1 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result.nextContinuationToken())
      .startAfter("dir1/key2")
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result1.isTruncated()));
    assertEquals(1, objectsV2Result1.contents().size());
    assertEquals(0, objectsV2Result1.commonPrefixes().size());
    assertEquals(1, objectsV2Result1.keyCount());
    assertEquals(1, objectsV2Result1.maxKeys());
    assertNotNull(objectsV2Result1.nextContinuationToken());
    assertNull(objectsV2Result1.prefix());
    assertNull(objectsV2Result1.delimiter());
    assertNull(objectsV2Result1.encodingType());
    assertEquals(objectsV2Result.nextContinuationToken(), objectsV2Result1.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir1/key2", objectsV2Result1.contents().get(0).key());

    // now, the continuation token is after "dir1@key2"
    ListObjectsV2Response objectsV2Result2 = s3.listObjectsV2(ListObjectsV2Request.builder()
      .bucket(bucketName)
      .continuationToken(objectsV2Result1.nextContinuationToken())
      .startAfter("dir1/key1")
      .maxKeys(1)
      .build());
    assertTrue(Boolean.TRUE.equals(objectsV2Result2.isTruncated()));
    assertEquals(1, objectsV2Result2.contents().size());
    assertEquals(0, objectsV2Result2.commonPrefixes().size());
    assertEquals(1, objectsV2Result2.keyCount());
    assertEquals(1, objectsV2Result2.maxKeys());
    assertNotNull(objectsV2Result2.nextContinuationToken());
    assertNull(objectsV2Result2.prefix());
    assertNull(objectsV2Result2.delimiter());
    assertNull(objectsV2Result2.encodingType());
    assertEquals(objectsV2Result1.nextContinuationToken(), objectsV2Result2.continuationToken());
    assertNull(objectsV2Result1.startAfter());
    assertEquals("dir2@key1", objectsV2Result2.contents().get(0).key());

    System.out.println("objectsV2Result = %s%n");
  }

  String prepareKeys(S3Client s3, String... keys) {
    String bucket = "test-list-objects" + System.currentTimeMillis();
    s3.createBucket(b -> b.bucket(bucket));
    for (String key : keys) {
      s3.putObject(b -> b.bucket(bucket).key(key), RequestBody.fromString("Content"));
    }
    return bucket;
  }

}
