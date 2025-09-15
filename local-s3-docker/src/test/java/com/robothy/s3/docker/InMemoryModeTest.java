package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.testcontainers.LocalS3Container;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Testcontainers
public class InMemoryModeTest {

  @Container
  public LocalS3Container container = new LocalS3Container("latest")
      .withMode(LocalS3Container.Mode.IN_MEMORY)
      .withRandomHttpPort();


  @Test
  void testInMemoryMode() {
    assertTrue(container.isRunning());
   // assertTrue(container.isHealthy());
    int port = container.getPort();

    S3Client s3Client = S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + port))
        .forcePathStyle(true)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("access-key-id", "secret-access-key")))
        .region(Region.AP_EAST_1)
        .build();

    assertDoesNotThrow(() -> s3Client.createBucket(b -> b.bucket("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(b -> b.bucket("my-bucket")));
    s3Client.close();
  }

}
