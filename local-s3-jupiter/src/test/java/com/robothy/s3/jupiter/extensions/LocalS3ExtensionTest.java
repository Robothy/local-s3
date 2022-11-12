package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import java.net.URL;
import org.junit.jupiter.api.Test;

@LocalS3
class LocalS3ExtensionTest {

  @Test
  void test(AmazonS3 s3Client, LocalS3Endpoint endpoint) {
    assertNotNull(s3Client);
    assertNotNull(endpoint);
    s3Client.createBucket("my-bucket");
  }

  @Test
  @LocalS3(port = 19090)
  void testOnMethod(AmazonS3 s3, LocalS3Endpoint endpoint) {
    Bucket bucket = s3.createBucket("my-bucket");
    s3.putObject(bucket.getName(), "hello.txt", "Hello");
    URL url = s3.getUrl(bucket.getName(), "hello.txt");
    assertEquals(19090, url.getPort());
    assertEquals("localhost", url.getHost());
    assertEquals(19090, endpoint.port());
  }

}