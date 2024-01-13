package com.robothy.s3.jupiter.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Method;
import java.util.Objects;

public abstract class AbstractLocalS3ParameterResolver implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return Objects.equals(className(), parameterContext.getParameter().getType().getName());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
      throws ParameterResolutionException {

    String methodName = context.getTestMethod().map(Method::toString).orElse("");
    String s3OnMethodKey = context.getRequiredTestClass()
        + (methodName + LocalS3Extension.LOCAL_S3_PORT_STORE_SUFFIX);
    String s3OnClassKey = context.getRequiredTestClass() + LocalS3Extension.LOCAL_S3_PORT_STORE_SUFFIX;
    ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
    Integer port = store.getOrDefault(s3OnMethodKey, Integer.TYPE, store.getOrDefault(s3OnClassKey, Integer.TYPE, null));
    if (null == port) {
      throw new IllegalStateException("You need to add the @LocalS3 annotation on your test class " +
          "or test method to inject a AmazonS3 instance.");
    }

    return resolve(port);
  }

  protected abstract String className();

  protected abstract Object resolve(int port);

}
