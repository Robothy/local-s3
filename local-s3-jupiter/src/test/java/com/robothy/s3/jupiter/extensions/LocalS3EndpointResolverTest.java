package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.*;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@LocalS3
class LocalS3EndpointResolverTest {

  @Order(1)
  @Test
  void test1(LocalS3Endpoint endpoint) {
    AmazonS3 client = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withEndpointConfiguration(endpoint.toAmazonS3EndpointConfiguration())
        .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
        .build();
    assertDoesNotThrow(() -> client.createBucket("my-bucket"));
  }

  @Order(2)
  @Test
  void test2(LocalS3Endpoint endpoint) {
    AmazonS3 client = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withEndpointConfiguration(endpoint.toAmazonS3EndpointConfiguration())
        .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
        .build();
    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("my-bucket")));
  }

}