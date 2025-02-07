package com.robothy.s3.test;

import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;


import java.util.List;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.core.ResponseBytes;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectTaggingIntegrationTest {

  @LocalS3
  @Test
  void testPutObjectTagging(S3Client s3) {
    String bucketName = "my-bucket";
    String key = "key1";

    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(),
              RequestBody.fromString("Hello"));

    assertDoesNotThrow(() -> s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build()));
    PutObjectTaggingResponse setObjectTaggingResult = s3.putObjectTagging(
      PutObjectTaggingRequest.builder().bucket(bucketName).key(key)
        .tagging(Tagging.builder().tagSet(List.of(Tag.builder().key("K1").value("V1").build(), Tag.builder().key("K2").value("V2").build())).build()).build());
    assertEquals("null", setObjectTaggingResult.versionId());

    GetObjectTaggingResponse objectTagging1 = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build());
    assertEquals(2, objectTagging1.tagSet().size());
    Tag tag1 = objectTagging1.tagSet().get(0);
    Tag tag2 = objectTagging1.tagSet().get(1);
    assertEquals("K1", tag1.key());
    assertEquals("V1", tag1.value());
    assertEquals("K2", tag2.key());
    assertEquals("V2", tag2.value());

    s3.putBucketVersioning(PutBucketVersioningRequest.builder().bucket(bucketName)
      .versioningConfiguration(VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build()).build());

    PutObjectResponse putObjectResult1 = s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromString("World"));
    assertDoesNotThrow(() -> s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build()));

    PutObjectTaggingResponse setObjectTaggingResult1 = s3.putObjectTagging(
      PutObjectTaggingRequest.builder().bucket(bucketName).key(key).versionId(putObjectResult1.versionId())
        .tagging(Tagging.builder().tagSet(List.of(Tag.builder().key("K3").value("V3").build())).build()).build());
    assertEquals(putObjectResult1.versionId(), setObjectTaggingResult1.versionId());

    assertDoesNotThrow(() -> s3.deleteObjectTagging(DeleteObjectTaggingRequest.builder().bucket(bucketName).key(key).build()));
  }

  @LocalS3
  @Test
  void testPutObjectWithTagging(S3Client s3) throws Exception {

    String bucketName = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key("key1")
                .tagging("K1=V1&K2=V2")
                .build(),
              RequestBody.fromString("Hello"));

    GetObjectTaggingResponse key1Tags = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key("key1").build());
    assertEquals(2, key1Tags.tagSet().size());
    assertEquals("K1", key1Tags.tagSet().get(0).key());
    assertEquals("V1", key1Tags.tagSet().get(0).value());
    assertEquals("K2", key1Tags.tagSet().get(1).key());
    assertEquals("V2", key1Tags.tagSet().get(1).value());
  }

  @LocalS3
  @Test
  void testPutObjectWithoutTagging(S3Client s3) {
    String bucketName = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key("key1")
                .build(),
              RequestBody.fromString("Hello"));
    GetObjectTaggingResponse objectTagging = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key("key1").build());
    assertEquals(0, objectTagging.tagSet().size());

    ResponseBytes<GetObjectResponse> object = s3.getObject(
      GetObjectRequest.builder().bucket(bucketName).key("key1").build(),
      ResponseTransformer.toBytes()
    );
    assertNull(object.response().tagCount());
  }

  @LocalS3
  @Test
  void testDeleteObjectTagging(S3Client s3) {
    String bucketName = "my-bucket";
    String key = "key1";

    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromString("Hello"));
    s3.putObjectTagging(PutObjectTaggingRequest.builder().bucket(bucketName).key(key)
      .tagging(Tagging.builder().tagSet(List.of(Tag.builder().key("K1").value("V1").build())).build()).build());

    GetObjectTaggingResponse objectTagging = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build());
    assertEquals(1, objectTagging.tagSet().size());

    DeleteObjectTaggingResponse deleteObjectTaggingResult = s3.deleteObjectTagging(DeleteObjectTaggingRequest.builder().bucket(bucketName).key(key).build());
    assertNull(deleteObjectTaggingResult.versionId());

    objectTagging = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build());
    assertEquals(0, objectTagging.tagSet().size());

    ResponseBytes<GetObjectResponse> object = s3.getObject(
      GetObjectRequest.builder().bucket(bucketName).key(key).build(),
      ResponseTransformer.toBytes()
    );
    assertNull(object.response().tagCount());

    s3.putBucketVersioning(PutBucketVersioningRequest.builder().bucket(bucketName)
      .versioningConfiguration(VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build()).build());
    PutObjectResponse putObjectResult = s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromString("World"));
    s3.putObjectTagging(PutObjectTaggingRequest.builder().bucket(bucketName).key(key).versionId(putObjectResult.versionId())
      .tagging(Tagging.builder().tagSet(List.of(Tag.builder().key("K1").value("V1").build())).build()).build());
    s3.deleteObjectTagging(DeleteObjectTaggingRequest.builder().bucket(bucketName).key(key).versionId(putObjectResult.versionId()).build());
    objectTagging = s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key(key).build());
    assertEquals(0, objectTagging.tagSet().size());
    assertEquals(putObjectResult.versionId(), objectTagging.versionId());

    ResponseBytes<GetObjectResponse> object1 = s3.getObject(
      GetObjectRequest.builder().bucket(bucketName).key(key).build(),
      ResponseTransformer.toBytes()
    );
    assertNull(object1.response().tagCount());
  }

}
