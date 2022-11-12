package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.robothy.s3.testcontainers.LocalS3Container;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class InMemoryModeTest {

  @Container
  public LocalS3Container container = new LocalS3Container("latest")
      .withMode(LocalS3Container.Mode.IN_MEMORY)
      .withRandomHttpPort();

  @Test
  void testInMemoryMode() {
    assertTrue(container.isRunning());
    int port = container.getPort();
    AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:" + port, "local"
        )).build();
    assertDoesNotThrow(() -> s3.createBucket("my-bucket"));
    assertDoesNotThrow(() -> s3.headBucket(new HeadBucketRequest("my-bucket")));
    s3.shutdown();
  }

}
