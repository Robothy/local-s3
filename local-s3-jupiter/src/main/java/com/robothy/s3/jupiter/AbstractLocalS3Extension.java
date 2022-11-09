package com.robothy.s3.jupiter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractLocalS3Extension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

  private static final Logger logger = LoggerFactory.getLogger(LocalS3Extension.class);

  private final ThreadLocal<com.robothy.s3.rest.LocalS3> localS3ForAll = new ThreadLocal<>();

  private final ThreadLocal<com.robothy.s3.rest.LocalS3> localS3ForEach = new ThreadLocal<>();

  public static final String LOCAL_S3_PORT_STORE_SUFFIX = ".LocalS3.Port";

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
  }

  @SneakyThrows
  private com.robothy.s3.rest.LocalS3 launch(LocalS3 s3Config) {
    com.robothy.s3.rest.LocalS3 localS3;
    int port = s3Config.port();
    if (port == -1) {
      port = findFreeTcpPort();
    }

    if (s3Config.inmemory()) {
      localS3 = com.robothy.s3.rest.LocalS3.builder()
          .port(port)
          .build();
    } else {
      Path dataDir = Files.createTempDirectory("local-s3");
      localS3 = com.robothy.s3.rest.LocalS3.builder()
          .port(port)
          .dataDirectory(dataDir)
          .build();
    }
    localS3.start();
    logger.debug("LocalS3 endpoint http://localhost:" + port);
    return localS3;
  }

  @SneakyThrows
  private void shutdown(com.robothy.s3.rest.LocalS3 localS3) {
    Path dataDirectory = localS3.getDataDirectory();
    localS3.shutdown();

    if (Objects.nonNull(dataDirectory)) {
      FileUtils.deleteDirectory(dataDirectory.toFile());
    }
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
