package com.robothy.s3.jupiter;

import com.amazonaws.client.builder.AwsClientBuilder;
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
public class AmazonS3Resolver implements ParameterResolver {

  private static final Logger log = LoggerFactory.getLogger(AmazonS3Resolver.class);

  private final AmazonS3ClientBuilder s3Builder = AmazonS3Client.builder()
      .enablePathStyleAccess();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == AmazonS3.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
      throws ParameterResolutionException {
    String s3OnMethodKey = context.getRequiredTestClass()
        + (context.getRequiredTestMethod() + LocalS3Extension.LOCAL_S3_PORT_STORE_SUFFIX);
    String s3OnClassKey = context.getRequiredTestClass() + LocalS3Extension.LOCAL_S3_PORT_STORE_SUFFIX;
    ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
    Integer port = store.getOrDefault(s3OnMethodKey, Integer.TYPE, store.getOrDefault(s3OnClassKey, Integer.TYPE, null));
    if (null == port) {
      throw new IllegalStateException("You need to add the @LocalS3 annotation on your test class " +
          "or test method to inject a AmazonS3 instance.");
    }

    String endpoint = "http://localhost:" + port;
    log.info("Inject AmazonS3 instance to " + context.getRequiredTestMethod() + " with endpoint " + endpoint + ".");

    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(endpoint, "local");
    return s3Builder.withEndpointConfiguration(endpointConfiguration).build();
  }

}
