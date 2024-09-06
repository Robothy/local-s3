package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.robothy.s3.jupiter.LocalS3;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class HeadObjectIntegrationTest {

  @Test
  @LocalS3
  void testHeadObject(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    s3.putObject(bucketName, "a.txt",  "Hello");

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.getUserMetadata().put("meta-key", "meta-value");
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "a.txt", "Hello")
        .withMetadata(metadata);

    s3.putObject(putObjectRequest);

    ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, "a.txt");
    assertTrue(objectMetadata.getUserMetadata().containsKey("meta-key"));
    assertEquals("meta-value", objectMetadata.getUserMetadata().get("meta-key"));

    assertTrue(s3.doesObjectExist(bucketName, "a.txt"));

    s3.deleteObject(bucketName, "a.txt");
    assertFalse(s3.doesObjectExist(bucketName, "a.txt"));
  }

  @Test
  @LocalS3
  void testGetVersionedObjectMetadata(S3Client s3Client) {
    s3Client.createBucket(builder -> builder.bucket("my-bucket"));
    s3Client.putBucketVersioning(builder -> builder.bucket("my-bucket").versioningConfiguration(c -> c.status("Enabled")));
    PutObjectResponse putObjectV1 = s3Client.putObject(builder -> builder.bucket("my-bucket")
            .key("a.txt")
            .metadata(Map.of("meta-k1", "meta-v1")),
        RequestBody.fromString("v1"));

    PutObjectResponse putObjectV2 = s3Client.putObject(builder -> builder.bucket("my-bucket")
            .key("a.txt")
            .metadata(Map.of("meta-k2", "meta-v2")),
        RequestBody.fromString("v2"));

    HeadObjectResponse headObjectResponse1 = s3Client.headObject(builder -> builder
        .bucket("my-bucket").key("a.txt").versionId(putObjectV1.versionId()));
    assertTrue(headObjectResponse1.metadata().containsKey("meta-k1"));
    assertEquals("meta-v1", headObjectResponse1.metadata().get("meta-k1"));

    HeadObjectResponse headObjectResponse2 = s3Client.headObject(builder -> builder
        .bucket("my-bucket").key("a.txt").versionId(putObjectV2.versionId()));
    assertTrue(headObjectResponse2.metadata().containsKey("meta-k2"));
    assertEquals("meta-v2", headObjectResponse2.metadata().get("meta-k2"));
  }


}
