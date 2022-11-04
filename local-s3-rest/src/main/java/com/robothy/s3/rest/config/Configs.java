package com.robothy.s3.rest.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class Configs {

  private static final Properties properties = new Properties();

  static {
    loadProperties();
  }

  public static String get(String name) {
    String property = get0(name);
    if (StringUtils.isBlank(property)) {
      throw new IllegalArgumentException("Cannot find property '" + name + "'.");
    }
    return property;
  }

  public static String get(String name, String defaultValue) {
    String property = get0(name);
    if (StringUtils.isBlank(property)) {
      return defaultValue;
    }
    return property;
  }

  private static String get0(String name) {
    Supplier<String>[] suppliers = new Supplier[]{
        () -> System.getProperty(name),
        () -> System.getenv(name),
        () -> properties.containsKey(name) ? properties.get(name).toString() : null
    };
    String property = null;
    for (Supplier<String> supplier : suppliers) {
      if (StringUtils.isNotBlank(property = supplier.get())) {
        break;
      }
    }
    return property;
  }

  @SneakyThrows
  private static void loadProperties() {
    InputStream in = Configs.class.getClassLoader().getResourceAsStream("local-s3.properties");
    if (in != null) {
      try (in) {
        properties.load(in);
      }
    }
  }

}
