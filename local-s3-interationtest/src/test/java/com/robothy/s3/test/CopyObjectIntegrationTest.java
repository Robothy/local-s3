package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.VersionListing;
import com.robothy.s3.jupiter.LocalS3;
import java.io.IOException;
import java.util.Date;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

public class CopyObjectIntegrationTest {

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
    assertTrue(copyObjectResult3.getLastModifiedDate().compareTo(new Date()) <= 0);
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
  void testCopyObjectWithSpecialCharactersInObjectKey(S3Client s3Client) {

    String bucketName = "robothy";
    s3Client.createBucket(b -> b.bucket(bucketName));

    String objectKeyWithPlusSign = "a%2Bb";
    s3Client.putObject(b -> b.bucket(bucketName).key(objectKeyWithPlusSign), RequestBody.fromString("Hello, World!"));
    s3Client.copyObject(b -> b.sourceBucket(bucketName).sourceKey(objectKeyWithPlusSign)
        .destinationBucket(bucketName).destinationKey("c d"));

  }

}
