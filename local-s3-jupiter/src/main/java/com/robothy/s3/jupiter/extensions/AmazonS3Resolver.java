package com.robothy.s3.jupiter.extensions;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.retry.RetryMode;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inject an AmazonS3 instance to a test parameter.
 */
public class AmazonS3Resolver extends AbstractLocalS3ParameterResolver<AmazonS3> {

  private final AmazonS3ClientBuilder s3Builder = AmazonS3Client.builder()
      .enablePathStyleAccess()
      .withClientConfiguration(new ClientConfiguration()
          .withConnectionTimeout(1000)
          .withSocketTimeout(1000));

  @Override
  protected Class<AmazonS3> type() {
    return AmazonS3.class;
  }

  @Override
  protected AmazonS3 resolve(int port) {
    String endpoint = "http://localhost:" + port;
    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(endpoint, "local");
    return s3Builder.withEndpointConfiguration(endpointConfiguration).build();
  }

}
