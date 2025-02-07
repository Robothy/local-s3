package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.Tag;
import com.robothy.s3.jupiter.LocalS3;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class PutObjectIntegrationTest {

  @Test
  @LocalS3
  void testPutObject(AmazonS3 s3) throws IOException {
    Bucket bucket1 = s3.createBucket("bucket1");
    assertFalse(s3.doesObjectExist("bucket1", "hello.txt"));
    s3.putObject(bucket1.getName(), "hello.txt", "Hello");
    assertTrue(s3.doesObjectExist("bucket1", "hello.txt"));
    S3Object object = s3.getObject(bucket1.getName(), "hello.txt");
    assertArrayEquals("Hello".getBytes(), object.getObjectContent().readAllBytes());
    assertEquals(DigestUtils.md5Hex("Hello"), object.getObjectMetadata().getETag());

    // test put object with tagging
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType("text/plain");
    metadata.addUserMetadata("key1", "value1");
    metadata.addUserMetadata("key2", "value2");
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucket1.getName(), "hello.txt",
        new ByteArrayInputStream("Robothy".getBytes()), metadata);
    ObjectTagging tagging = new ObjectTagging(List.of(new Tag("key1", "value1"), new Tag("key2",
        "value2")));
    putObjectRequest.setTagging(tagging);
    PutObjectResult putObjectResult = s3.putObject(putObjectRequest);
    assertNotNull(putObjectResult);
    S3Object object1 = s3.getObject(bucket1.getName(), "hello.txt");
    Map<String, String> userMetadata = object1.getObjectMetadata().getUserMetadata();
    assertEquals(2, userMetadata.size());
    assertEquals("value1", userMetadata.get("key1"));
    assertEquals("value2", userMetadata.get("key2"));
    assertArrayEquals("Robothy".getBytes(), object1.getObjectContent().readAllBytes());
    GetObjectTaggingRequest getObjectTaggingRequest = new GetObjectTaggingRequest(bucket1.getName(),
        "hello.txt");
    GetObjectTaggingResult objectTaggingResult = s3.getObjectTagging(getObjectTaggingRequest);
    assertNotNull(objectTaggingResult);
    assertEquals(2, objectTaggingResult.getTagSet().size());
    Tag tag1 = objectTaggingResult.getTagSet().get(0);
    assertEquals("key1", tag1.getKey());
    assertEquals("value1", tag1.getValue());
    Tag tag2 = objectTaggingResult.getTagSet().get(1);
    assertEquals("key2", tag2.getKey());
    assertEquals("value2", tag2.getValue());
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
