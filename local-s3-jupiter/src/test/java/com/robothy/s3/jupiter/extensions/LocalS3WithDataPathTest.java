package com.robothy.s3.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.S3Object;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocalS3WithDataPathTest {


  @TempDir
  private static File tmpDir;

  @Order(1)
  @Test
  @LocalS3(mode = LocalS3Mode.PERSISTENCE, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Create a bucket and a object in PERSISTENCE mode.")
  void test1(AmazonS3 client) {
    assertDoesNotThrow(() -> client.createBucket("my-bucket"));
    assertDoesNotThrow(() -> client.putObject("my-bucket", "a.txt", "LocalS3"));
  }

  @Order(2)
  @Test
  @LocalS3(mode = LocalS3Mode.IN_MEMORY, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Create a bucket and a object in IN_MEMORY mode.")
  void test2(AmazonS3 client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("my-bucket")));
    S3Object object = client.getObject("my-bucket", "a.txt");
    assertEquals("LocalS3", new String(object.getObjectContent().readAllBytes()));
    assertDoesNotThrow(() -> client.createBucket("your-bucket"));
    assertDoesNotThrow(() -> client.putObject("your-bucket", "b.txt", "Robothy"));
  }

  @Order(3)
  @Test
  @LocalS3(mode = LocalS3Mode.PERSISTENCE, dataPathSupplier = DataPathSupplierImpl.class)
  @DisplayName("Changes in IN_MEMORY mode won't affect data in the disk.")
  void test3(AmazonS3 client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("my-bucket")));
    S3Object object = client.getObject("my-bucket", "a.txt");
    assertEquals("LocalS3", new String(object.getObjectContent().readAllBytes()));
    // todo https://github.com/Robothy/local-s3/issues/10
    //assertThrows(AmazonClientException.class, () -> client.headBucket(new HeadBucketRequest("your-bucket")));
    assertDoesNotThrow(() -> client.createBucket("her-bucket"));
    assertDoesNotThrow(() -> client.putObject("her-bucket", "c.txt", "Hello"));
  }

  @Order(4)
  @Test
  @LocalS3(mode = LocalS3Mode.IN_MEMORY, dataPathSupplier = DataPathSupplierImpl.class, initialDataCacheEnabled = false)
  @DisplayName("Change in PERSISTENCE mode will be persisted.")
  void test4(AmazonS3 client) throws IOException {
    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("my-bucket")));
    S3Object object = client.getObject("my-bucket", "a.txt");
    assertEquals("LocalS3", new String(object.getObjectContent().readAllBytes()));

    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("her-bucket")));
    S3Object object1 = client.getObject("her-bucket", "c.txt");
    assertEquals("Hello", new String(object1.getObjectContent().readAllBytes()));
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
