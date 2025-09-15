package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import java.net.URI;

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
    S3Client client = S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + localS3.getPort()))
        .forcePathStyle(true)
        .region(Region.AP_EAST_1)
        .build();

    String bucketName = "init-bucket";
    String key = "a.txt";
    client.createBucket(builder -> builder.bucket(bucketName));
    client.putObject(builder -> builder.bucket(bucketName).key(key), RequestBody.fromString("Hello"));
    client.close();
    localS3.shutdown();
    tmpDir.deleteOnExit();
  }

  @Container
  public LocalS3Container localS3Container = new LocalS3Container("latest")
      .withRandomHttpPort()
      .withDataPath(tmpDir.getAbsolutePath())
      .withMode(LocalS3Container.Mode.IN_MEMORY);

  private final S3Client s3 = S3Client.builder()
      .endpointOverride(URI.create("http://localhost:" + localS3Container.getPort()))
      .forcePathStyle(true)
      .region(Region.AP_EAST_1)
      .build();

  @Order(1)
  @Test
  @DisplayName("Read initial data.")
  public void testInMemoryModeWithInitialData() throws IOException {
    assertDoesNotThrow(() -> s3.headBucket(builder -> builder.bucket("init-bucket")));
    var objectResponse = s3.getObjectAsBytes(builder -> builder.bucket("init-bucket").key("a.txt"));
    assertEquals("Hello", objectResponse.asUtf8String());

    assertDoesNotThrow(() -> s3.createBucket(builder -> builder.bucket("my-bucket")));
    assertDoesNotThrow(() -> s3.putObject(builder -> builder.bucket("my-bucket").key("b.txt"), RequestBody.fromString("Robothy")));
  }

  @Order(2)
  @Test
  @DisplayName("Initial data shouldn't be affected.")
  public void inMemoryLocalS3ShouldNotAffectInitialData() throws IOException {
    assertDoesNotThrow(() -> s3.headBucket(builder -> builder.bucket("init-bucket"))); // bucket from initial data.
    assertThrows(NoSuchBucketException.class, () -> s3.headBucket(builder -> builder.bucket("my-bucket"))); // bucket created in the previous test.
    var objectResponse = s3.getObjectAsBytes(builder -> builder.bucket("init-bucket").key("a.txt"));
    assertEquals("Hello", objectResponse.asUtf8String());
  }

}
