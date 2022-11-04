package com.robothy.s3.rest.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ConfigsTest {

  void get() {
    assertThrows(IllegalArgumentException.class, () -> Configs.get(ConfigNames.EXECUTOR_THREAD));
    assertEquals("abc", Configs.get(ConfigNames.EXECUTOR_THREAD, "abc"));
    System.setProperty(ConfigNames.EXECUTOR_THREAD, "3");
    assertEquals("3", Configs.get(ConfigNames.EXECUTOR_THREAD));
    assertEquals("80", Configs.get(ConfigNames.PORT));
    System.setProperty(ConfigNames.PORT, "18080");
    assertEquals("18080", Configs.get(ConfigNames.PORT));
  }
}