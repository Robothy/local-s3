package io.github.robothy.s3.extensions;

import io.github.robothy.s3.RealS3;
import java.net.URI;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.S3VectorsClientBuilder;

/**
 * Parameter resolver for injecting configured S3VectorsClient instances into test methods
 * annotated with @RealS3.
 */
public class RealS3VectorsClientResolver extends AbstractRealS3ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return S3VectorsClient.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  @Override
  protected Object createClient(RealS3 config) {
    S3VectorsClientBuilder builder = S3VectorsClient.builder()
        .region(Region.of(config.region()))
        .credentialsProvider(createCredentialsProvider(config));

    // Set custom endpoint if provided
    if (!config.endpointUrl().isEmpty()) {
      builder.endpointOverride(URI.create(config.endpointUrl()));
    }

    // Note: S3VectorsClient does not support path style access configuration
    // The pathStyleAccess parameter is ignored for S3VectorsClient

    return builder.build();
  }
}
