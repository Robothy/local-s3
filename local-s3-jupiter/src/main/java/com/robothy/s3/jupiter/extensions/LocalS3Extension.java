package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.supplier.DataPathSupplier;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.Objects;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalS3Extension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

  private static final Logger logger = LoggerFactory.getLogger(LocalS3Extension.class);

  private static final ThreadLocal<com.robothy.s3.rest.LocalS3> localS3ForAll = new ThreadLocal<>();

  private static final ThreadLocal<com.robothy.s3.rest.LocalS3> localS3ForEach = new ThreadLocal<>();

  public static final String LOCAL_S3_PORT_STORE_SUFFIX = ".LocalS3.Port";

  public static final String AMAZON_S3_REGION_STORE_SUFFIX = ".AmazonS3.Region";

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    LocalS3 s3Config = context.getRequiredTestClass().getAnnotation(LocalS3.class);
    if (s3Config != null) {
      com.robothy.s3.rest.LocalS3 localS3 = launch(s3Config);
      localS3ForAll.set(localS3);
      String key = context.getRequiredTestClass() + LOCAL_S3_PORT_STORE_SUFFIX;
      context.getStore(ExtensionContext.Namespace.GLOBAL).put(key, localS3.getPort());
    }

  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Method testMethod = context.getRequiredTestMethod();
    LocalS3 s3Config = testMethod.getAnnotation(LocalS3.class);
    if (s3Config != null) {
      com.robothy.s3.rest.LocalS3 localS3 = launch(s3Config);
      localS3ForEach.set(localS3);
      String key = context.getRequiredTestClass() + (context.getRequiredTestMethod() + LOCAL_S3_PORT_STORE_SUFFIX);
      context.getStore(ExtensionContext.Namespace.GLOBAL).put(key, localS3.getPort());
    }

  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (Objects.nonNull(localS3ForEach.get())) {
      shutdown(localS3ForEach.get());
      localS3ForEach.remove();
      String key = context.getRequiredTestClass() + (context.getRequiredTestMethod() + LOCAL_S3_PORT_STORE_SUFFIX);
      context.getStore(ExtensionContext.Namespace.GLOBAL).remove(key);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    if (Objects.nonNull(localS3ForAll.get())) {
      shutdown(localS3ForAll.get());
      localS3ForAll.remove();
    }

    String key = context.getRequiredTestClass() + AMAZON_S3_REGION_STORE_SUFFIX;
    context.getStore(ExtensionContext.Namespace.GLOBAL).remove(key);
  }

  @SneakyThrows
  private com.robothy.s3.rest.LocalS3 launch(LocalS3 s3Config) {
    int port = s3Config.port();
    if (port == -1) {
      port = findFreeTcpPort();
    }

    String dataPath = s3Config.dataPath();
    if (StringUtils.isBlank(dataPath) && s3Config.dataPathSupplier() != DataPathSupplier.class) {
      try {
        Constructor<? extends DataPathSupplier> constructor = s3Config.dataPathSupplier().getDeclaredConstructor();
        DataPathSupplier dataPathSupplier = constructor.newInstance();
        dataPath = dataPathSupplier.get();
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException("You must define no-args constructor in " + s3Config.dataPathSupplier());
      }
    }

    com.robothy.s3.rest.LocalS3 localS3 = com.robothy.s3.rest.LocalS3.builder()
        .port(port)
        .dataPath(StringUtils.isBlank(dataPath) ? null : dataPath)
        .mode(s3Config.mode())
        .initialDataCacheEnabled(s3Config.initialDataCacheEnabled())
        .build();
    localS3.start();
    logger.debug("LocalS3 endpoint http://localhost:" + port);
    return localS3;
  }

  @SneakyThrows
  private void shutdown(com.robothy.s3.rest.LocalS3 localS3) {
    localS3.shutdown();
  }

  private int findFreeTcpPort() {
    int freePort;
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      freePort = serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("TCP port is not available.");
    }
    return freePort;
  }

}