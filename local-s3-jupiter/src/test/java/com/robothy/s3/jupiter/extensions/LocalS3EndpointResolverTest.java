package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@LocalS3
class LocalS3EndpointResolverTest {

  @Order(1)
  @Test
  void test1(LocalS3Endpoint endpoint) {
    S3Client client = S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + endpoint.port()))
        .forcePathStyle(true)
        .region(Region.AP_EAST_1)
        .build();
    assertDoesNotThrow(() -> client.createBucket(builder -> builder.bucket("my-bucket")));
  }

  @Order(2)
  @Test
  void test2(LocalS3Endpoint endpoint) {
    S3Client client = S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + endpoint.port()))
        .forcePathStyle(true)
        .region(Region.AP_EAST_1)
        .build();
    assertDoesNotThrow(() -> client.headBucket(builder -> builder.bucket("my-bucket")));
  }

}