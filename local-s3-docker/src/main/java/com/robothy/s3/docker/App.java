package com.robothy.s3.docker;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  private static final String MODE = "MODE";

  public static void main(String[] args) {
    if (getProperty(MODE) == null) {
      log.info("\"MODE\" is not specified; use the default value \"PERSISTENCE\"");
    }

    final String mode = Optional.ofNullable(getProperty(MODE)).orElse(LocalS3Mode.PERSISTENCE.name());
    if (Arrays.stream(LocalS3Mode.values()).noneMatch(m -> m.name().equalsIgnoreCase(mode))) {
      log.error("\"{}\" is not a valid mode. Valid values are {}", mode, LocalS3Mode.values());
      System.exit(1);
    }

    log.info("Starting LocalS3 in {} mode.", mode);

    LocalS3.builder()
        .port(80)
        .mode(LocalS3Mode.valueOf(mode.toUpperCase()))
        .dataPath("/data")
        .build()
        .start();
  }

  private static String getProperty(String name) {
    return Optional.ofNullable(System.getenv(name)).orElse(System.getProperty(name));
  }

}
