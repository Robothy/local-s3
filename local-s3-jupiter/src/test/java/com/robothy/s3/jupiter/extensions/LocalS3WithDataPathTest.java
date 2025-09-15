package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.supplier.DataPathSupplier;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocalS3WithDataPathTest {


  @TempDir
  private static File tmpDir;

  @Order(1)
  @Test
  @LocalS3(mode = LocalS3Mode.PERSISTENCE, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Create a bucket and a object in PERSISTENCE mode.")
  void test1(S3Client client) {
    assertDoesNotThrow(() -> client.createBucket(b -> b.bucket("my-bucket")));
    assertDoesNotThrow(() -> client.putObject(b -> b.bucket("my-bucket").key("a.txt").build(), RequestBody.fromString("LocalS3")));
  }

  @Order(2)
  @Test
  @LocalS3(mode = LocalS3Mode.IN_MEMORY, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Create a bucket and a object in IN_MEMORY mode.")
  void test2(S3Client client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(b -> b.bucket("my-bucket")));
    var objectResponse = client.getObjectAsBytes(b -> b.bucket("my-bucket").key("a.txt"));
    assertEquals("LocalS3", objectResponse.asUtf8String());
    assertDoesNotThrow(() -> client.createBucket(b -> b.bucket("your-bucket")));
    assertDoesNotThrow(() -> client.putObject(b -> b.bucket("your-bucket").key("b.txt"), RequestBody.fromString("Robothy")));
  }

  @Order(3)
  @Test
  @LocalS3(mode = LocalS3Mode.PERSISTENCE, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Changes in IN_MEMORY mode won't affect data in the disk.")
  void test3(S3Client client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(b -> b.bucket("my-bucket")));
    var objectResponse = client.getObjectAsBytes(b -> b.bucket("my-bucket").key("a.txt"));
    assertEquals("LocalS3", objectResponse.asUtf8String());
    // todo https://github.com/Robothy/local-s3/issues/10
    //assertThrows(AmazonClientException.class, () -> client.headBucket(new HeadBucketRequest("your-bucket")));
    assertDoesNotThrow(() -> client.createBucket(b -> b.bucket("her-bucket")));
    assertDoesNotThrow(() -> client.putObject(b -> b.bucket("her-bucket").key("c.txt"), RequestBody.fromString("Hello")));
  }

  @Order(4)
  @Test
  @LocalS3(mode = LocalS3Mode.IN_MEMORY, dataPathSupplier = DataPathSupplierImpl.class, initialDataCacheEnabled = false)
  @DisplayName("Change in PERSISTENCE mode will be persisted.")
  void test4(S3Client client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(b -> b.bucket("my-bucket")));
    var objectResponse = client.getObjectAsBytes(b -> b.bucket("my-bucket").key("a.txt"));
    assertEquals("LocalS3", objectResponse.asUtf8String());

    assertDoesNotThrow(() -> client.headBucket(b -> b.bucket("her-bucket")));
    var objectResponse1 = client.getObjectAsBytes(b -> b.bucket("her-bucket").key("c.txt"));
    assertEquals("Hello", objectResponse1.asUtf8String());
  }

  static class DataPathSupplierImpl implements DataPathSupplier {
    @Override
    public String get() {
      return tmpDir.getAbsolutePath();
    }
  }

  @AfterAll
  public static void cleanup() throws IOException {
    FileUtils.deleteDirectory(tmpDir);
  }

}
