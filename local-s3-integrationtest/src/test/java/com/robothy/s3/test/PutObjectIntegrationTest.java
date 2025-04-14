package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.*;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4UnsignedPayloadSigner;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.auth.signer.S3SignerExecutionAttribute;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.auth.aws.crt.internal.signer.AwsChunkedV4aPayloadSigner;
import software.amazon.awssdk.http.auth.aws.crt.internal.signer.V4aPayloadSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4aHttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.internal.handlers.EnableChunkedEncodingInterceptor;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Map;
import software.amazon.awssdk.utils.AttributeMap;

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


  @Test
  @LocalS3
  void test_STREAMING_AWS4_HMAC_SHA256_PAYLOAD_TRAILER(LocalS3Endpoint endpoint) throws Exception {

    try (S3Client s3Client = S3Client.builder()
        .region(Region.of(endpoint.region()))
        .overrideConfiguration(ClientOverrideConfiguration.builder()
            .putAdvancedOption(SdkAdvancedClientOption.DISABLE_HOST_PREFIX_INJECTION, true)
            .build())
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .endpointOverride(URI.create(endpoint.endpoint()))
        .httpClient(ApacheHttpClient.builder().buildWithDefaults(AttributeMap.builder()
            .put(TRUST_ALL_CERTIFICATES, Boolean.TRUE)
            .build()))
        .build()) {

      String bucketName = "my-bucket";
      String key = "hello.txt";
      byte[] content = new byte[1024 * 1024 * 6];
      s3Client.createBucket(b -> b.bucket(bucketName));
      s3Client.putObject(b -> b.bucket(bucketName).key(key)
              .contentLength((long) content.length)
              .contentType("text/plain"),
          RequestBody.fromBytes(content));

      ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(b -> b.bucket(bucketName).key(key));
      assertArrayEquals(content, object.asByteArray());
    }

  }


  @Test
  @LocalS3
  void test_STREAMING_AWS4_HMAC_SHA256_PAYLOAD(S3Client s3Client) throws Exception {
    String bucketName = "my-bucket";
    String key = "hello.txt";
    byte[] content = new byte[1024 * 1024 * 6];
    s3Client.createBucket(b -> b.bucket(bucketName));
    s3Client.putObject(b -> b.bucket(bucketName).key(key)
            .overrideConfiguration(c -> {
              c.signer(AwsS3V4Signer.create());
              c.executionAttributes()
                  .putAttribute(S3SignerExecutionAttribute.ENABLE_PAYLOAD_SIGNING, true)
                  .putAttribute(S3SignerExecutionAttribute.ENABLE_CHUNKED_ENCODING, true)
              ;
              c.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")));
            })
            .contentLength((long) content.length)
            .contentType("text/plain"),
        RequestBody.fromBytes(content));

    ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(b -> b.bucket(bucketName).key(key));
    assertArrayEquals(content, object.asByteArray());
  }


  @Test
  @LocalS3
  void test_UNSIGNED_PAYLOAD(S3Client s3Client) throws Exception {
    String bucketName = "my-bucket";
    String key = "hello.txt";
    byte[] content = new byte[1024 * 1024 * 6];
    s3Client.createBucket(b -> b.bucket(bucketName));
    s3Client.putObject(b -> b.bucket(bucketName).key(key)
            .overrideConfiguration(c -> {
              c.signer(Aws4UnsignedPayloadSigner.create());
              c.executionAttributes()
                  .putAttribute(S3SignerExecutionAttribute.ENABLE_PAYLOAD_SIGNING, true)
                  .putAttribute(S3SignerExecutionAttribute.ENABLE_CHUNKED_ENCODING, true)
              ;
              c.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")));
            })
            .contentLength((long) content.length)
            .contentType("text/plain"),
        RequestBody.fromBytes(content));

    ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(b -> b.bucket(bucketName).key(key));
    assertArrayEquals(content, object.asByteArray());
  }

  //@Test
  //@LocalS3
  void test_STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD(S3Client s3Client) throws Exception {
    // TODO: implement this test
  }

//  @Test
//  @LocalS3
  void test_STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD_TAILER(LocalS3Endpoint localS3Endpoint) throws Exception {
    // TODO: implement this test
  }


}
