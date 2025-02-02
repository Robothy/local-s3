package com.robothy.s3.jupiter.extensions;

import static com.robothy.s3.jupiter.extensions.LocalS3Extension.AMAZON_S3_REGION_STORE_SUFFIX;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class RealS3ClientResolver implements ParameterResolver {

  private static final String AWS_ACCESS_KEY_ENV = "AWS_ACCESS_KEY_ID";
  private static final String AWS_SECRET_KEY_ENV = "AWS_SECRET_ACCESS_KEY";

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return S3Client.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  private StaticCredentialsProvider getCredentialsProvider() {
    String accessKey = System.getenv(AWS_ACCESS_KEY_ENV);
    String secretKey = System.getenv(AWS_SECRET_KEY_ENV);

    if (accessKey == null || secretKey == null) {
      throw new IllegalStateException(
          String.format("Environment variables %s and %s must be set",
              AWS_ACCESS_KEY_ENV, AWS_SECRET_KEY_ENV));
    }

    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey));
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    String key = extensionContext.getRequiredTestClass() + AMAZON_S3_REGION_STORE_SUFFIX;
    String region = (String) extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get(key);
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(getCredentialsProvider())
        .build();
  }
}
