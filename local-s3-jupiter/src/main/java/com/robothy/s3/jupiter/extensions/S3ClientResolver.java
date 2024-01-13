package com.robothy.s3.jupiter.extensions;

import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

public class S3ClientResolver extends AbstractLocalS3ParameterResolver {

  @Override
  protected String className() {
    return "software.amazon.awssdk.services.s3.S3Client";
  }

  @SneakyThrows
  @Override
  protected Object resolve(int port) {
    String endpoint = "http://localhost:" + port;

    return S3Client.builder()
      .forcePathStyle(true)
      .endpointOverride(new URI(endpoint))
      .region(Region.of("local"))
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .build();
  }

}
