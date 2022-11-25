package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MultiLocalS3InstanceIntegrationTest {

  @Test
  @DisplayName("Multiple LocalS3 instance in the same JVM")
  void test() throws IOException {
    Path dataPath = Files.createTempDirectory("local-s3");

    LocalS3 persistenceInstance = LocalS3.builder()
        .mode(LocalS3Mode.PERSISTENCE)
        .dataPath(dataPath.toAbsolutePath().toString())
        .port(-1)
        .build();

    persistenceInstance.start();
    LocalS3 inMemoryInstance = LocalS3.builder()
        .mode(LocalS3Mode.IN_MEMORY)
        .port(-1)
        .build();
    inMemoryInstance.start();

    AmazonS3 persistenceS3 = createClient(persistenceInstance.getPort());
    AmazonS3 inMemoS3 = createClient(inMemoryInstance.getPort());
    assertDoesNotThrow(() -> persistenceS3.createBucket("my-bucket"));
    assertDoesNotThrow(() -> inMemoS3.createBucket("my-bucket"));

    persistenceInstance.shutdown();
    inMemoryInstance.shutdown();

    LocalS3 inMemoWithInitial = LocalS3.builder().mode(LocalS3Mode.IN_MEMORY)
        .dataPath(dataPath.toAbsolutePath().toString())
        .port(-1)
        .build();
    inMemoWithInitial.start();
    AmazonS3 client = createClient(inMemoWithInitial.getPort());
    assertDoesNotThrow(() -> client.headBucket(new HeadBucketRequest("my-bucket")));

  }

  AmazonS3 createClient(int port) {
    return AmazonS3ClientBuilder.standard().withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, "local"))
        .enablePathStyleAccess()
        .build();
  }

}
