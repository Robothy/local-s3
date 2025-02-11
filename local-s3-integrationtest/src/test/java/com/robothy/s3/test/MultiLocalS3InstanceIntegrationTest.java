package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

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

    S3Client persistenceS3 = createClient(persistenceInstance.getPort());
    S3Client inMemoS3 = createClient(inMemoryInstance.getPort());
    assertDoesNotThrow(() -> persistenceS3.createBucket(
        CreateBucketRequest.builder()
            .bucket("my-bucket")
            .build()
    ));
    assertDoesNotThrow(() -> inMemoS3.createBucket(
        CreateBucketRequest.builder()
            .bucket("my-bucket")
            .build()
    ));

    persistenceInstance.shutdown();
    inMemoryInstance.shutdown();

    LocalS3 inMemoWithInitial = LocalS3.builder().mode(LocalS3Mode.IN_MEMORY)
        .dataPath(dataPath.toAbsolutePath().toString())
        .port(-1)
        .build();
    inMemoWithInitial.start();
    S3Client client = createClient(inMemoWithInitial.getPort());
    assertDoesNotThrow(() -> client.headBucket(
        HeadBucketRequest.builder()
            .bucket("my-bucket")
            .build()
    ));

  }

  S3Client createClient(int port) {
    return S3Client.builder()
        .endpointOverride(java.net.URI.create("http://localhost:" + port))
        .region(Region.of("local"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create("foo", "bar")
        ))
        .forcePathStyle(true)
        .build();
  }

}
