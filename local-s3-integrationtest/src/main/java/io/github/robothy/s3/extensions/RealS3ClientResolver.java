package io.github.robothy.s3.extensions;

import io.github.robothy.s3.RealS3;
import java.net.URI;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Parameter resolver for injecting configured S3Client instances into test methods
 * annotated with @RealS3.
 */
public class RealS3ClientResolver extends AbstractRealS3ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return S3Client.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  @Override
  protected Object createClient(RealS3 config) {
    S3ClientBuilder builder = S3Client.builder()
        .region(Region.of(config.region()))
        .credentialsProvider(createCredentialsProvider(config));

    // Set custom endpoint if provided
    if (!config.endpointUrl().isEmpty()) {
      builder.endpointOverride(URI.create(config.endpointUrl()));
    }

    // Set path style access if requested
    if (config.pathStyleAccess()) {
      builder.forcePathStyle(true);
    }

    return builder.build();
  }
}
