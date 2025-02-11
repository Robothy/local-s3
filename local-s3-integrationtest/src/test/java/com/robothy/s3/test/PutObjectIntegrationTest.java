package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Map;

public class PutObjectIntegrationTest {

  @Test
  @LocalS3
  void testPutObject(S3Client s3) throws IOException {
    String bucketName = "bucket1";
    s3.createBucket(b -> b.bucket(bucketName));
    assertThrows(NoSuchKeyException.class, () -> s3.headObject(b -> b.bucket(bucketName).key("hello.txt")).hasMetadata());
    s3.putObject(b -> b.bucket(bucketName).key("hello.txt"), RequestBody.fromString("Hello"));
    assertTrue(s3.headObject(b -> b.bucket(bucketName).key("hello.txt")).hasMetadata());
    ResponseBytes<GetObjectResponse> object = s3.getObjectAsBytes(b -> b.bucket(bucketName).key("hello.txt"));
    assertArrayEquals("Hello".getBytes(), object.asByteArray());
    assertEquals(DigestUtils.md5Hex("Hello"), object.response().eTag());

    // test put object with tagging
    Map<String, String> metadata = Map.of("key1", "value1", "key2", "value2");
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key("hello.txt")
        .contentType("text/plain")
        .metadata(metadata)
        .tagging(Tagging.builder().tagSet(
            Tag.builder().key("key1").value("value1").build(),
            Tag.builder().key("key2").value("value2").build()
        ).build())
        .build();

    s3.putObject(putObjectRequest, RequestBody.fromString("Robothy"));
    ResponseBytes<GetObjectResponse> object1 = s3.getObjectAsBytes(b -> b.bucket(bucketName).key("hello.txt"));
    Map<String, String> userMetadata = object1.response().metadata();
    assertEquals(2, userMetadata.size());
    assertEquals("value1", userMetadata.get("key1"));
    assertEquals("value2", userMetadata.get("key2"));
    assertArrayEquals("Robothy".getBytes(), object1.asByteArray());
    GetObjectTaggingResponse objectTaggingResult = s3.getObjectTagging(GetObjectTaggingRequest.builder()
        .bucket(bucketName)
        .key("hello.txt")
        .build());
    assertNotNull(objectTaggingResult);
    assertEquals(2, objectTaggingResult.tagSet().size());
    Tag tag1 = objectTaggingResult.tagSet().get(0);
    assertEquals("key1", tag1.key());
    assertEquals("value1", tag1.value());
    Tag tag2 = objectTaggingResult.tagSet().get(1);
    assertEquals("key2", tag2.key());
    assertEquals("value2", tag2.value());
  }

  @Test
  @LocalS3
  void testPutObjectWithSpecialCharactersInObjectKey(S3Client s3Client) {
    String bucketName = "my-bucket";
    s3Client.createBucket(b -> b.bucket(bucketName));

    String objectKeyWithPlusSign = "hello+world.txt";
    s3Client.putObject(b -> b.bucket(bucketName).key(objectKeyWithPlusSign), RequestBody.fromString("Hello World"));
    assertDoesNotThrow(() -> s3Client.headObject(b -> b.bucket(bucketName).key(objectKeyWithPlusSign)));
  }

  @Test
  @LocalS3
  void testPutObjectWithS3Client(S3Client client) throws Exception {
    String bucketName = "my-bucket";
    client.createBucket(b -> b.bucket(bucketName));
    String objectKey = "hello.txt";
    String content = "Hello";
    for (int i = 0; i < 13; i++) {
      content += content;
    }

    String finalContent = content;
    client.putObject(b -> b.bucket(bucketName).key("hello.txt")
        .contentLength((long) finalContent.length()), RequestBody.fromString(content));
    ResponseBytes<GetObjectResponse> gotObjectAsBytes = client.getObjectAsBytes(b -> b.bucket(bucketName).key(objectKey));
    System.out.println("Content-Length: " + content.length());
    assertEquals(content, gotObjectAsBytes.asUtf8String());
  }

}
