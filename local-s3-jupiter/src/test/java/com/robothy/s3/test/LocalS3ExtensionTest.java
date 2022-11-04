package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.robothy.s3.jupiter.LocalS3;
import java.net.URL;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;

@LocalS3
class LocalS3ExtensionTest {

  @Test
  void test(AmazonS3 s3Client) {
    assertNotNull(s3Client);
    s3Client.createBucket("my-bucket");
  }

  @Test
  @LocalS3(port = 19090)
  void testOnMethod(AmazonS3 s3) {
    Bucket bucket = s3.createBucket("my-bucket");
    s3.putObject(bucket.getName(), "hello.txt", "Hello");
    URL url = s3.getUrl(bucket.getName(), "hello.txt");
    assertEquals(19090, url.getPort());
    assertEquals("localhost", url.getHost());
  }

}