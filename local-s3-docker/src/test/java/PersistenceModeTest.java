import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.robothy.s3.testcontainer.LocalS3Container;
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

  @Order(1)
  @Test
  public void create() {
    assertTrue(container.isRunning());
    AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:" + container.getPort(), "local"
        )).build();
    String bucket = "my-bucket";
    assertDoesNotThrow(() -> s3.createBucket("my-bucket"));
    assertDoesNotThrow(() -> s3.putObject(bucket, "a.txt", "Hello World"));
    s3.shutdown();
  }

  @Order(2)
  @Test
  public void read() throws IOException {
    assertTrue(container.isRunning());
    AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:" + container.getPort(), "local"
        )).build();
    String bucket = "my-bucket";
    S3Object object = s3.getObject(bucket, "a.txt");
    assertEquals("Hello World", new String(object.getObjectContent().readAllBytes()));
    s3.shutdown();
  }

}
