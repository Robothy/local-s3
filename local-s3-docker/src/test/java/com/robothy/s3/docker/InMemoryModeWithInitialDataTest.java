package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import com.robothy.s3.testcontainers.LocalS3Container;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@DisplayName("Run LocalS3 container with initial data.")
public class InMemoryModeWithInitialDataTest {

  private static File tmpDir = Files.createTempDir();

  @BeforeAll
  public static void setup() {
    // prepare initial data.
    LocalS3 localS3 = LocalS3.builder()
        .mode(LocalS3Mode.PERSISTENCE)
        .dataPath(tmpDir.getAbsolutePath())
        .port(-1)
        .build();
    localS3.start();
    AmazonS3 client = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + localS3.getPort(), "local"))
        .build();

    String bucketName = "init-bucket";
    String key = "a.txt";
    client.createBucket(bucketName);
    client.putObject(bucketName, key, "Hello");
    client.shutdown();
    localS3.shutdown();
    tmpDir.deleteOnExit();
  }

  @Container
  public LocalS3Container localS3Container = new LocalS3Container("latest")
      .withRandomHttpPort()
      .withDataPath(tmpDir.getAbsolutePath())
      .withMode(LocalS3Container.Mode.IN_MEMORY);

  private final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
      .enablePathStyleAccess()
      .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
      .withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration("http://localhost:" + localS3Container.getPort(), "local"))
      .build();

  @Order(1)
  @Test
  @DisplayName("Read initial data.")
  public void testInMemoryModeWithInitialData() throws IOException {
    assertDoesNotThrow(() -> s3.headBucket(new HeadBucketRequest("init-bucket")));
    S3Object object = s3.getObject("init-bucket", "a.txt");
    assertEquals("Hello", new String(object.getObjectContent().readAllBytes()));

    assertDoesNotThrow(() -> s3.createBucket("my-bucket"));
    assertDoesNotThrow(() -> s3.putObject("my-bucket", "b.txt", "Robothy"));
  }

  @Order(2)
  @Test
  @DisplayName("Initial data shouldn't be affected.")
  public void inMemoryLocalS3ShouldNotAffectInitialData() throws IOException {
    assertDoesNotThrow(() -> s3.headBucket(new HeadBucketRequest("init-bucket"))); // bucket from initial data.
    assertThrows(AmazonClientException.class, () -> s3.headBucket(new HeadBucketRequest("my-bucket"))); // bucket created in the previous test.
    S3Object object = s3.getObject("init-bucket", "a.txt");
    assertEquals("Hello", new String(object.getObjectContent().readAllBytes()));
  }

}
