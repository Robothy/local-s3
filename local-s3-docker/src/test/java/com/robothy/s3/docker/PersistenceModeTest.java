package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.testcontainers.LocalS3Container;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class PersistenceModeTest {

  private static final File tmpDir = Files.createTempDir();

  @Container
  private final LocalS3Container container = new LocalS3Container("latest")
      .withRandomHttpPort()
      .withDataPath(tmpDir.getAbsolutePath());

  @AfterAll
  public static void setup() {
    tmpDir.deleteOnExit();
  }

  S3Client createS3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + container.getPort()))
        .forcePathStyle(true)
        .region(Region.AP_EAST_1)
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .build();
  }

  @Order(1)
  @Test
  public void create() {
    assertTrue(container.isRunning());
    try (S3Client s3 = createS3Client()) {
      String bucket = "my-bucket";
      assertDoesNotThrow(() -> s3.createBucket(builder -> builder.bucket("my-bucket")));
      assertDoesNotThrow(
          () -> s3.putObject(builder -> builder.bucket(bucket).key("a.txt"), RequestBody.fromString("Hello World")));
    }
  }

  @Order(2)
  @Test
  public void read() throws IOException {
    assertTrue(container.isRunning());
    try (S3Client s3 = createS3Client()) {
      String bucket = "my-bucket";
      var objectResponse = s3.getObjectAsBytes(builder -> builder.bucket(bucket).key("a.txt"));
      assertEquals("Hello World", objectResponse.asUtf8String());
    }
  }

}
