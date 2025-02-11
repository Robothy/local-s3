package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.jupiter.LocalS3;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class HeadObjectIntegrationTest {

  @Test
  @LocalS3
  void testHeadObject(S3Client s3Client) {
    s3Client.createBucket(builder -> builder.bucket("my-bucket"));
    s3Client.putObject(builder -> builder.bucket("my-bucket").key("a.txt"), RequestBody.fromString("Hello"));

    PutObjectResponse putObjectResponse = s3Client.putObject(builder -> builder.bucket("my-bucket")
            .key("a.txt")
            .metadata(Map.of("meta-key", "meta-value")),
        RequestBody.fromString("Hello"));

    HeadObjectResponse headObjectResponse = s3Client.headObject(builder -> builder.bucket("my-bucket").key("a.txt"));
    assertTrue(headObjectResponse.metadata().containsKey("meta-key"));
    assertEquals("meta-value", headObjectResponse.metadata().get("meta-key"));

    assertDoesNotThrow(() -> s3Client.headObject(builder -> builder.bucket("my-bucket").key("a.txt")));
    s3Client.deleteObject(builder -> builder.bucket("my-bucket").key("a.txt"));
    assertThrows(NoSuchKeyException.class, () -> s3Client.headObject(builder -> builder.bucket("my-bucket").key("a.txt")));
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
