package com.robothy.s3.test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectTaggingIntegrationTest {

  @LocalS3
  @Test
  void testPutObjectTagging(AmazonS3 s3) {
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
  void testPutObjectWithTagging(AmazonS3 s3) throws Exception {

    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "key1", new ByteArrayInputStream("Hello".getBytes()), new ObjectMetadata())
      .withTagging(new ObjectTagging(List.of(new Tag("K1", "V1"), new Tag("K2", "V2"))));
    s3.putObject(putObjectRequest);

    S3Object s3Object = s3.getObject(bucketName, "key1");
    assertEquals(2, s3Object.getTaggingCount());

    GetObjectTaggingResult key1Tags = s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, "key1"));
    assertEquals(2, key1Tags.getTagSet().size());
    assertEquals("K1", key1Tags.getTagSet().get(0).getKey());
    assertEquals("V1", key1Tags.getTagSet().get(0).getValue());
    assertEquals("K2", key1Tags.getTagSet().get(1).getKey());
    assertEquals("V2", key1Tags.getTagSet().get(1).getValue());
  }

  @LocalS3
  @Test
  void testPutObjectWithoutTagging(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    s3.putObject(bucketName, "key1", "Hello");
    GetObjectTaggingResult objectTagging = s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, "key1"));
    assertEquals(0, objectTagging.getTagSet().size());

    S3Object object = s3.getObject(bucketName, "key1");
    assertNull(object.getTaggingCount());
  }

  @LocalS3
  @Test
  void testDeleteObjectTagging(AmazonS3 s3) {
    String bucketName = "my-bucket";
    String key = "key1";

    s3.createBucket(bucketName);
    s3.putObject(bucketName, key, "Hello");
    s3.setObjectTagging(new SetObjectTaggingRequest(bucketName, key, new ObjectTagging(List.of(new Tag("K1", "V1")))));

    GetObjectTaggingResult objectTagging = s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, key));
    assertEquals(1, objectTagging.getTagSet().size());

    DeleteObjectTaggingResult deleteObjectTaggingResult = s3.deleteObjectTagging(new DeleteObjectTaggingRequest(bucketName, key));
    assertNull(deleteObjectTaggingResult.getVersionId());

    objectTagging = s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, key));
    assertEquals(0, objectTagging.getTagSet().size());

    S3Object object = s3.getObject(bucketName, key);
    assertNull(object.getTaggingCount());
  }

}
