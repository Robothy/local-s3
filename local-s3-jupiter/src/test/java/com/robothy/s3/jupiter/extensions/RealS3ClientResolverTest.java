package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.jupiter.AmzS3;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class RealS3ClientResolverTest {

  @BeforeAll
  static void beforeAll() {
    System.setProperty(RealS3ClientResolver.AWS_ACCESS_KEY_ENV, "fake");
    System.setProperty(RealS3ClientResolver.AWS_SECRET_KEY_ENV, "fake");
  }

  @AmzS3
  @Test
  void test(S3Client client) {
    assertNotNull(client);
  }


  @AfterAll
  static void afterAll() {
    System.clearProperty(RealS3ClientResolver.AWS_ACCESS_KEY_ENV);
    System.clearProperty(RealS3ClientResolver.AWS_SECRET_KEY_ENV);
  }
}