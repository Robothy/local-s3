package com.robothy.s3.jupiter;

import com.amazonaws.client.builder.AwsClientBuilder;
import java.util.Objects;

/**
 * Represent the endpoint information of a LocalS3 service.
 */
public class LocalS3Endpoint {

  private final int port;

  private final String endpoint;

  private final String region;

  public LocalS3Endpoint(int port) {
    this.port = port;
    this.region = "local";
    this.endpoint = "http://localhost:" + port;
  }

  public int port() {
    return port;
  }

  public String endpoint() {
    return endpoint;
  }

  public String region() {
    return region;
  }

  /**
   * Create an {@linkplain com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration} instance by
   * {@linkplain LocalS3Endpoint}.
   *
   * @return a {@code EndpointConfiguration} instance.
   */
  public AwsClientBuilder.EndpointConfiguration toAmazonS3EndpointConfiguration() {
    return new AwsClientBuilder.EndpointConfiguration(endpoint, region);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalS3Endpoint that = (LocalS3Endpoint) o;
    return port == that.port && Objects.equals(endpoint, that.endpoint) && Objects.equals(region, that.region);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, endpoint, region);
  }
}
