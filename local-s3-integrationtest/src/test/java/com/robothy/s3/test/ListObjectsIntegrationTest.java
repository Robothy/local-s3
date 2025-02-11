package com.robothy.s3.test;

import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class ListObjectsIntegrationTest {

  @Test
  @LocalS3
  @DisplayName("Test List Objects")
  void testListObjects(S3Client s3) {
    CreateBucketResponse bucket = s3.createBucket(req -> req.bucket("my-bucket"));
    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket("my-bucket"));
    assertEquals("my-bucket", objectListing.name());
    assertTrue(objectListing.contents().isEmpty());
    assertTrue(objectListing.commonPrefixes().isEmpty());
    assertNull(objectListing.nextMarker());
    assertNull(objectListing.delimiter());
    assertEquals("", objectListing.prefix());

    s3.putObject(req -> req.bucket("my-bucket").key("a.txt"), 
                RequestBody.fromString("Hello"));
    objectListing = s3.listObjects(req -> req.bucket("my-bucket"));
    assertEquals("my-bucket", objectListing.name());
    assertEquals(1, objectListing.contents().size());
    assertTrue(objectListing.commonPrefixes().isEmpty());
    assertNull(objectListing.nextMarker());
    assertNull(objectListing.delimiter());
    S3Object keyObj = objectListing.contents().get(0);
    assertEquals("a.txt", keyObj.key());
    assertEquals(DigestUtils.md5Hex("Hello"), keyObj.eTag().replace("\"", ""));
    assertEquals(5, keyObj.size());
    assertEquals("STANDARD", keyObj.storageClass().toString());
    assertNotNull(keyObj.lastModified());
    assertNotNull(keyObj.owner());

    // Continue with directory tests
    s3.putObject(req -> req.bucket("my-bucket").key("dir1/a.txt"), 
                RequestBody.fromString("Content A"));
    s3.putObject(req -> req.bucket("my-bucket").key("dir1/b.txt"), 
                RequestBody.fromString("Content B"));
    s3.putObject(req -> req.bucket("my-bucket").key("dir2/c.txt"), 
                RequestBody.fromString("Content C"));
    
    objectListing = s3.listObjects(req -> req.bucket("my-bucket").prefix("dir1/"));
    assertEquals("my-bucket", objectListing.name());
    assertEquals(2, objectListing.contents().size());
    assertEquals("dir1/a.txt", objectListing.contents().get(0).key());
    assertEquals(DigestUtils.md5Hex("Content A"), 
                objectListing.contents().get(0).eTag().replace("\"", ""));
    assertEquals("dir1/b.txt", objectListing.contents().get(1).key());
    assertEquals(DigestUtils.md5Hex("Content B"), 
                objectListing.contents().get(1).eTag().replace("\"", ""));
    assertEquals(0, objectListing.commonPrefixes().size());

    // Test pagination
    ListObjectsResponse objectListing1 = s3.listObjects(req -> req
      .bucket("my-bucket")
      .maxKeys(1));
    assertEquals("my-bucket", objectListing1.name());
    assertEquals(1, objectListing1.contents().size());
    assertEquals("a.txt", objectListing1.contents().get(0).key());
    assertEquals(0, objectListing1.commonPrefixes().size());
    assertTrue(objectListing1.isTruncated());
    assertEquals("a.txt", objectListing1.nextMarker());

    ListObjectsResponse objectListing2 = s3.listObjects(req -> req
      .bucket("my-bucket")
      .marker(objectListing1.contents().get(0).key())
      .maxKeys(2));
    assertEquals(2, objectListing2.contents().size());
    assertEquals("dir1/a.txt", objectListing2.contents().get(0).key());
    assertEquals("dir1/b.txt", objectListing2.contents().get(1).key());
    assertEquals(0, objectListing2.commonPrefixes().size());
    assertTrue(objectListing2.isTruncated());
  }

  @Test
  @LocalS3
  void listObjectsWithBucketVersioning(S3Client s3) {
    String bucketName = "my-bucket";
    String key = "a.txt";
    s3.createBucket(req -> req.bucket(bucketName));
    s3.putObject(req -> req.bucket(bucketName).key(key), 
                RequestBody.fromString("Text1"));

    s3.putBucketVersioning(req -> req.bucket(bucketName)
      .versioningConfiguration(conf -> conf.status(BucketVersioningStatus.ENABLED)));
    
    s3.putObject(req -> req.bucket(bucketName).key(key),
                RequestBody.fromString("Text2"));
                
    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName));
    assertEquals(1, objectListing.contents().size());

    s3.putObject(req -> req.bucket(bucketName).key("dir1/a.txt"),
                RequestBody.fromString("Text3"));
    s3.putObject(req -> req.bucket(bucketName).key("dir1/b.txt"),
                RequestBody.fromString("Text4"));

    ListObjectsResponse objectListing1 = s3.listObjects(req -> req.bucket(bucketName));
    assertEquals(3, objectListing1.contents().size());

    ListObjectsResponse objectListing2 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("/").maxKeys(10));
    assertEquals(1, objectListing2.contents().size());
    assertEquals(1, objectListing2.commonPrefixes().size());
    assertEquals(10, objectListing2.maxKeys());

    s3.deleteObject(req -> req.bucket(bucketName).key(key));
    ListObjectsResponse objectListing3 = s3.listObjects(req -> req.bucket(bucketName));
    assertEquals(2, objectListing3.contents().size());
    assertEquals(0, objectListing3.commonPrefixes().size());
  }

  @Test
  @LocalS3
  void testListObjectsKeyEncoding(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName)
      .encodingType(EncodingType.URL));
    assertFalse(objectListing.isTruncated());
    assertEquals(1000, objectListing.maxKeys());
    assertEquals(4, objectListing.contents().size());
    assertEquals(0, objectListing.commonPrefixes().size());
    assertEquals("", objectListing.prefix());
    assertNull(objectListing.delimiter());
    assertEquals("", objectListing.marker());
    assertNull(objectListing.nextMarker());
    assertEquals("url", objectListing.encodingType().toString().toLowerCase());
    assertEquals("dir1/key1", objectListing.contents().get(0).key());
    assertEquals("dir1/key2", objectListing.contents().get(1).key());
    assertEquals("dir2@key1", objectListing.contents().get(2).key());
    assertEquals("dir2@key2", objectListing.contents().get(3).key());

    // Test with prefix
    ListObjectsResponse objectListing1 = s3.listObjects(req -> req.bucket(bucketName)
      .encodingType(EncodingType.URL).prefix("dir2@"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(2, objectListing1.contents().size());
    assertEquals("dir2@key1", objectListing1.contents().get(0).key());
    assertEquals("dir2@key2", objectListing1.contents().get(1).key());

    // Test with delimiter
    ListObjectsResponse objectListing2 = s3.listObjects(req -> req.bucket(bucketName)
      .encodingType(EncodingType.URL).delimiter("@k"));
    assertEquals(2, objectListing2.contents().size());
    assertEquals(1, objectListing2.commonPrefixes().size());
    assertEquals("dir2@k", objectListing2.commonPrefixes().get(0).prefix());

    // Test invalid encoding type
    assertThrows(S3Exception.class, () -> s3.listObjects(req -> req.bucket(bucketName)
      .encodingType("invalid")));
  }

  @Test
  @LocalS3
  void testListObjectsDelimiter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("/").maxKeys(1));
    assertTrue(objectListing.isTruncated());
    assertEquals(1, objectListing.maxKeys());
    assertEquals(0, objectListing.contents().size());
    assertEquals(1, objectListing.commonPrefixes().size());
    assertEquals("dir1/", objectListing.commonPrefixes().get(0).prefix());
  }

  @Test
  @LocalS3
  void testListObjectsDelimiterMarker(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing1 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("/").maxKeys(1).marker("dir1/"));
    assertTrue(objectListing1.isTruncated());
    assertEquals(1, objectListing1.maxKeys());
    assertEquals(1, objectListing1.contents().size());
    assertEquals(0, objectListing1.commonPrefixes().size());
    assertEquals("", objectListing1.prefix());
    assertEquals("/", objectListing1.delimiter());
    assertEquals("dir1/", objectListing1.marker());
    assertEquals("dir2@key1", objectListing1.contents().get(0).key());

    ListObjectsResponse objectListing2 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("/").maxKeys(1).marker("dir2@key1"));
    assertFalse(objectListing2.isTruncated());
    assertEquals(1, objectListing2.maxKeys());
    assertEquals(1, objectListing2.contents().size());
    assertEquals(0, objectListing2.commonPrefixes().size());
    assertEquals("", objectListing2.prefix());
    assertEquals("/", objectListing2.delimiter());
    assertEquals("dir2@key1", objectListing2.marker());
    assertEquals("dir2@key2", objectListing2.contents().get(0).key());

    ListObjectsResponse objectListing3 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("@").maxKeys(1).marker("dir1/key2"));
    assertFalse(objectListing3.isTruncated());
    assertEquals(1, objectListing3.maxKeys());
    assertEquals(0, objectListing3.contents().size());
    assertEquals(1, objectListing3.commonPrefixes().size());
    assertEquals("", objectListing3.prefix());
    assertEquals("@", objectListing3.delimiter());
    assertEquals("dir1/key2", objectListing3.marker());
    assertEquals("dir2@", objectListing3.commonPrefixes().get(0).prefix());
  }

  @Test
  @LocalS3
  void testListObjectsDelimiterMaxKeys(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");
    
    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("k").maxKeys(1));
    assertTrue(objectListing.isTruncated());
    assertEquals(1, objectListing.maxKeys());
    assertEquals(0, objectListing.contents().size());
    assertEquals(1, objectListing.commonPrefixes().size());
    assertEquals("", objectListing.prefix());
    assertEquals("k", objectListing.delimiter());
    assertEquals("", objectListing.marker());
    assertEquals("dir1/k", objectListing.commonPrefixes().get(0).prefix());
  }

  @Test
  @LocalS3
  void testListObjectsPrefix(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName)
      .prefix("dir1/"));
    assertFalse(objectListing.isTruncated());
    assertEquals(2, objectListing.contents().size());
    assertEquals(0, objectListing.commonPrefixes().size());
    assertEquals("dir1/", objectListing.prefix());
    assertNull(objectListing.delimiter());
    assertEquals("", objectListing.marker());
    assertEquals("dir1/key1", objectListing.contents().get(0).key());
    assertEquals("dir1/key2", objectListing.contents().get(1).key());
  }

  @Test
  @LocalS3
  void testListObjectsPrefixDelimiter(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing1 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("k").prefix("dir1"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(0, objectListing1.contents().size());
    assertEquals(1, objectListing1.commonPrefixes().size());
    assertEquals("dir1", objectListing1.prefix());
    assertEquals("k", objectListing1.delimiter());
    assertEquals("dir1/k", objectListing1.commonPrefixes().get(0).prefix());

    ListObjectsResponse objectListing2 = s3.listObjects(req -> req.bucket(bucketName)
      .delimiter("d").prefix("dir1"));
    assertFalse(objectListing2.isTruncated());
    assertEquals(2, objectListing2.contents().size());
    assertEquals(0, objectListing2.commonPrefixes().size());
    assertEquals("dir1", objectListing2.prefix());
    assertEquals("d", objectListing2.delimiter());
    assertEquals("dir1/key1", objectListing2.contents().get(0).key());
    assertEquals("dir1/key2", objectListing2.contents().get(1).key());
  }

  @Test
  @LocalS3
  void testListObjectsPrefixMarker(S3Client s3) {
    String bucketName = prepareKeys(s3, "dir1/key1", "dir1/key2", "dir2@key1", "dir2@key2");

    ListObjectsResponse objectListing = s3.listObjects(req -> req.bucket(bucketName)
      .prefix("dir1/").marker("dir1/key2"));
    assertFalse(objectListing.isTruncated());
    assertEquals(0, objectListing.contents().size());
    assertEquals(0, objectListing.commonPrefixes().size());
    assertEquals("dir1/", objectListing.prefix());
    assertNull(objectListing.delimiter());
    assertEquals("dir1/key2", objectListing.marker());

    ListObjectsResponse objectListing1 = s3.listObjects(req -> req.bucket(bucketName)
      .prefix("dir1/").marker("dir1/key1"));
    assertFalse(objectListing1.isTruncated());
    assertEquals(1, objectListing1.contents().size());
    assertEquals(0, objectListing1.commonPrefixes().size());
    assertEquals("dir1/", objectListing1.prefix());
    assertNull(objectListing1.delimiter());
    assertEquals("dir1/key1", objectListing1.marker());
    assertEquals("dir1/key2", objectListing1.contents().get(0).key());
  }

  private String prepareKeys(S3Client s3, String... keys) {
    String bucket = "test-list-objects" + System.currentTimeMillis();
    s3.createBucket(req -> req.bucket(bucket));
    for (String key : keys) {
      s3.putObject(req -> req.bucket(bucket).key(key),
                  RequestBody.fromString("Content"));
    }
    return bucket;
  }
}
