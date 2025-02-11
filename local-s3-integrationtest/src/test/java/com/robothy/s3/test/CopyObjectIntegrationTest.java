package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.jupiter.LocalS3;
import java.io.IOException;
import java.util.Date;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.ResponseInputStream;


public class CopyObjectIntegrationTest {

  @LocalS3
  @Test
  void testCopyObject(S3Client s3) throws IOException {
    String bucket1 = "bucket1";
    String key1 = "key1";
    String bucket2 = "bucket2";
    String key2 = "key2";
    String text = "Robothy";

    s3.createBucket(b -> b.bucket(bucket1));
    s3.putObject(b -> b.bucket(bucket1).key(key1), RequestBody.fromString(text));
    s3.createBucket(b -> b.bucket(bucket2));

    /*-- Copy from a versioning disabled bucket to another versioning disabled bucket. --*/
    CopyObjectResponse copyObjectResult1 = s3.copyObject(b -> b.sourceBucket(bucket1).sourceKey(key1).destinationBucket(bucket2).destinationKey(key2));
    assertEquals("null", copyObjectResult1.versionId());
    assertTrue(copyObjectResult1.copyObjectResult().lastModified().isBefore(new Date().toInstant()));
    try (ResponseInputStream<GetObjectResponse> response1 = s3.getObject(b -> b.bucket(bucket2).key(key2))) {
      GetObjectResponse object1 = response1.response();
      assertEquals(text.length(), object1.contentLength());
      assertEquals(text, new String(response1.readAllBytes()));
    }

    /*-- Copy from a versioning disabled bucket to a versioning enabled bucket. --*/
    s3.putBucketVersioning(b -> b.bucket(bucket2).versioningConfiguration(c -> c.status(BucketVersioningStatus.ENABLED)));
    CopyObjectResponse copyObjectResult2 = s3.copyObject(b -> b.sourceBucket(bucket1).sourceKey(key1).destinationBucket(bucket2).destinationKey(key2));
    assertNotEquals("null", copyObjectResult2.versionId());
    assertTrue(copyObjectResult2.copyObjectResult().lastModified().isAfter(copyObjectResult1.copyObjectResult().lastModified()));
    try (ResponseInputStream<GetObjectResponse> response2 = s3.getObject(b -> b.bucket(bucket2).key(key2))) {
      GetObjectResponse object2 = response2.response();
      assertEquals(text.length(), object2.contentLength());
      assertEquals(text, new String(response2.readAllBytes()));
    }
    ListObjectVersionsResponse versionListing1 = s3.listObjectVersions(b -> b.bucket(bucket2).prefix(key2));
    assertEquals(2, versionListing1.versions().size());
    assertEquals(copyObjectResult2.versionId(), versionListing1.versions().get(0).versionId());
    assertEquals("null", versionListing1.versions().get(1).versionId());

    /*-- Copy from a versioning enabled to a versioning disabled bucket. --*/

    // copy without source version ID.
    String text2 = "LocalS3";
    PutObjectResponse putObjectResult1 = s3.putObject(b -> b.bucket(bucket2).key(key2), RequestBody.fromString(text2));
    CopyObjectResponse copyObjectResult3 = s3.copyObject(b -> b.sourceBucket(bucket2).sourceKey(key2).destinationBucket(bucket1).destinationKey(key1));
    assertEquals("null", copyObjectResult3.versionId());
    assertTrue(copyObjectResult3.copyObjectResult().lastModified().compareTo(new Date().toInstant()) <= 0);
    try (ResponseInputStream<GetObjectResponse> response3 = s3.getObject(b -> b.bucket(bucket1).key(key1))) {
      GetObjectResponse object3 = response3.response();
      assertEquals(text2.length(), object3.contentLength());
      assertEquals(text2, new String(response3.readAllBytes()));
    }

    // copy with source version ID.
    CopyObjectResponse copyObjectResult4 = s3.copyObject(CopyObjectRequest.builder()
        .sourceBucket(bucket2)
        .sourceKey(key2)
        .sourceVersionId(putObjectResult1.versionId())
        .destinationBucket(bucket1)
        .destinationKey(key1)
        .build());
    assertEquals("null", copyObjectResult4.versionId());
    try (ResponseInputStream<GetObjectResponse> response4 = s3.getObject(b -> b.bucket(bucket1).key(key1))) {
      GetObjectResponse object4 = response4.response();
      assertEquals(text2.length(), object4.contentLength());
      assertEquals(text2, new String(response4.readAllBytes()));
    }
  }

  @LocalS3
  @Test
  void testCopyObjectWithSpecialCharactersInObjectKey(S3Client s3Client) {

    String bucketName = "robothy";
    s3Client.createBucket(b -> b.bucket(bucketName));

    String objectKeyWithPlusSign = "a%2Bb";
    s3Client.putObject(b -> b.bucket(bucketName).key(objectKeyWithPlusSign), RequestBody.fromString("Hello, World!"));
    s3Client.copyObject(b -> b.sourceBucket(bucketName).sourceKey(objectKeyWithPlusSign)
        .destinationBucket(bucketName).destinationKey("c d"));

  }

}
