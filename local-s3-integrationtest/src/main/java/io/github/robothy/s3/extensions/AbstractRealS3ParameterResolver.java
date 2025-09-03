package io.github.robothy.s3.extensions;

import io.github.robothy.s3.RealS3;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

/**
 * Abstract base class for RealS3 parameter resolvers that provides common functionality
 * for creating AWS clients with proper credential resolution.
 */
public abstract class AbstractRealS3ParameterResolver implements ParameterResolver {

  public static final String AWS_ACCESS_KEY_ENV = "AWS_ACCESS_KEY_ID";
  public static final String AWS_SECRET_KEY_ENV = "AWS_SECRET_ACCESS_KEY";
  public static final String AWS_SESSION_TOKEN_ENV = "AWS_SESSION_TOKEN";

  @Override
  public final Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    RealS3 config = RealS3Extension.getRealS3Config(extensionContext);
    if (config == null) {
      throw new ParameterResolutionException("@RealS3 annotation not found on test method");
    }

    return createClient(config);
  }

  /**
   * Creates the specific AWS client based on the provided configuration.
   * Subclasses should implement this method to create their specific client type.
   *
   * @param config the RealS3 configuration
   * @return the configured AWS client
   */
  protected abstract Object createClient(RealS3 config);

  /**
   * Creates an AWS credentials provider based on the RealS3 configuration.
   * Prefers annotation values over environment variables, falls back to default provider chain.
   *
   * @param config the RealS3 configuration
   * @return configured credentials provider
   */
  protected final AwsCredentialsProvider createCredentialsProvider(RealS3 config) {
    // First try to get credentials from annotation
    String accessKey = !config.accessKey().isEmpty() ? config.accessKey() : 
        Optional.ofNullable(System.getenv(AWS_ACCESS_KEY_ENV))
            .orElse(System.getProperty(AWS_ACCESS_KEY_ENV));
    
    String secretKey = !config.secretKey().isEmpty() ? config.secretKey() : 
        Optional.ofNullable(System.getenv(AWS_SECRET_KEY_ENV))
            .orElse(System.getProperty(AWS_SECRET_KEY_ENV));

    String sessionToken = !config.sessionToken().isEmpty() ? config.sessionToken() : 
        Optional.ofNullable(System.getenv(AWS_SESSION_TOKEN_ENV))
            .orElse(System.getProperty(AWS_SESSION_TOKEN_ENV));

    // If we have explicit credentials, use them
    if (accessKey != null && secretKey != null) {
      AwsCredentials credentials;
      if (sessionToken != null && !sessionToken.isEmpty()) {
        credentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
      } else {
        credentials = AwsBasicCredentials.create(accessKey, secretKey);
      }
      return StaticCredentialsProvider.create(credentials);
    }

    // Fall back to default credentials provider chain
    // This will try environment variables, system properties, profile files, IAM roles, etc.
    return DefaultCredentialsProvider.create();
  }
}
