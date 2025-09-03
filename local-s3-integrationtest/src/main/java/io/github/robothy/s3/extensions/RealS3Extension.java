package io.github.robothy.s3.extensions;

import io.github.robothy.s3.RealS3;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit5 extension that manages RealS3 configuration for test methods.
 * This extension extracts configuration from @RealS3 annotations and stores
 * them in the ExtensionContext for use by parameter resolvers.
 */
public class RealS3Extension implements BeforeEachCallback {

  private static final Logger logger = LoggerFactory.getLogger(RealS3Extension.class);

  public static final String REAL_S3_CONFIG_STORE_KEY = "RealS3.Config";

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Method testMethod = context.getRequiredTestMethod();
    RealS3 s3Config = testMethod.getAnnotation(RealS3.class);
    
    if (s3Config != null) {
      logger.debug("Setting up RealS3 configuration for test method: {}", testMethod.getName());
      
      // Store the configuration in the extension context for parameter resolvers to access
      String key = context.getRequiredTestClass() + "." + context.getRequiredTestMethod().getName() + "." + REAL_S3_CONFIG_STORE_KEY;
      context.getStore(ExtensionContext.Namespace.GLOBAL).put(key, s3Config);
      
      logger.debug("RealS3 configured with region: {}, pathStyleAccess: {}, endpointUrl: {}", 
          s3Config.region(), s3Config.pathStyleAccess(), 
          s3Config.endpointUrl().isEmpty() ? "default" : s3Config.endpointUrl());
    }
  }

  /**
   * Retrieves the RealS3 configuration for the current test context.
   *
   * @param context the extension context
   * @return the RealS3 configuration, or null if not found
   */
  public static RealS3 getRealS3Config(ExtensionContext context) {
    String key = context.getRequiredTestClass() + "." + context.getRequiredTestMethod().getName() + "." + REAL_S3_CONFIG_STORE_KEY;
    return (RealS3) context.getStore(ExtensionContext.Namespace.GLOBAL).get(key);
  }
}
