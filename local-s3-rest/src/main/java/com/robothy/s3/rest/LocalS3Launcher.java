package com.robothy.s3.rest;

import com.robothy.s3.rest.bootstrap.LocalS3Bootstrap;
import com.robothy.s3.rest.bootstrap.Mode;
import com.robothy.s3.rest.config.ConfigNames;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import lombok.SneakyThrows;

public class LocalS3Launcher {

  public static void main(String[] args) {
    if (args.length == 0 || (!Mode.PERSISTENCE.name().equalsIgnoreCase(args[0])
        && !Mode.IN_MEMORY.name().equalsIgnoreCase(args[0]))) {
      System.out.println("Usage:    java -jar local-s3.jar [persist | in_memory]");
      System.exit(0);
    }

    System.setProperty(ConfigNames.PORT, "18080");
    Properties properties = loadProperties();
    LocalS3Bootstrap.bootstrap(Mode.valueOf(args[0].toUpperCase(Locale.ROOT)), properties)
        .start();
  }

  @SneakyThrows
  private static Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream in = LocalS3Launcher.class.getClassLoader().getResourceAsStream("local-s3.properties")) {
      if (in != null) {
        properties.load(in);
      }
    }

    String[] names = new String[] {
        ConfigNames.PORT,
        ConfigNames.EXECUTOR_THREAD,
        ConfigNames.ROOT_DIRECTORY,
        ConfigNames.NETTY_PARENT_EVENT_LOOP_GROUP_THREAD,
        ConfigNames.NETTY_CHILD_EVENT_LOOP_GROUP_THREAD
    };

    for (String name : names) {
      Object value = get(name);
      if (Objects.nonNull(value)) {
        properties.put(name, value);
      }
    }

    return properties;
  }

  private static Object get(String name) {
    Object value = System.getProperty(name);
    if (null != value) {
      return value;
    }
    return System.getenv(name);
  }

}
