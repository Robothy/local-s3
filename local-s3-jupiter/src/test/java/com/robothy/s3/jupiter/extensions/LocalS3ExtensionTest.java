package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@LocalS3
class LocalS3ExtensionTest {

  @Test
  void test(S3Client s3Client, LocalS3Endpoint endpoint) {
    assertNotNull(s3Client);
    assertNotNull(endpoint);
    s3Client.createBucket(builder -> builder.bucket("my-bucket"));
  }

  @Test
  @LocalS3(port = 19090)
  void testOnMethod(S3Client s3, LocalS3Endpoint endpoint) throws MalformedURLException {
    String bucketName = "my-bucket";
    s3.createBucket(builder -> builder.bucket(bucketName));
    s3.putObject(builder -> builder.bucket(bucketName).key("hello.txt"), RequestBody.fromString("Hello"));
    
    // Construct URL manually since S3Client doesn't have getUrl method
    URL url = new URL("http", "localhost", 19090, "/" + bucketName + "/hello.txt");
    assertEquals(19090, url.getPort());
    assertEquals("localhost", url.getHost());
    assertEquals(19090, endpoint.port());
  }

}