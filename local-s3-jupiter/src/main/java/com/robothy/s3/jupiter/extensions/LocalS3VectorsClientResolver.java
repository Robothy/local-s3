package com.robothy.s3.jupiter.extensions;

import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;

import java.net.URI;

/**
 * Parameter resolver for injecting configured S3VectorsClient instances into test methods
 * annotated with @LocalS3.
 */
public class LocalS3VectorsClientResolver extends AbstractLocalS3ParameterResolver {

  @Override
  protected String className() {
    return "software.amazon.awssdk.services.s3vectors.S3VectorsClient";
  }

  @SneakyThrows
  @Override
  protected Object resolve(int port) {
    String endpoint = "http://localhost:" + port;

    return S3VectorsClient.builder()
      .endpointOverride(new URI(endpoint))
      .region(Region.of("local"))
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .build();
  }

}
