package com.robothy.s3.testcontainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LocalS3ContainerTest {

  @Test
  void test() {

    try (LocalS3Container container = new LocalS3Container("latest")) {
      container.withDataPath("/data/local-s3")
          .withHttpPort(8080)
          .withMode(LocalS3Container.Mode.IN_MEMORY);
      assertTrue(container.getBinds().stream().anyMatch(bind -> bind.getPath().equals("/data/local-s3")
          && bind.getVolume().getPath().equals("/data")));
      assertTrue(container.getPortBindings().contains("8080:80/tcp"));
      assertTrue(container.getEnvMap().containsKey("MODE"));
      assertEquals(LocalS3Container.Mode.IN_MEMORY.name(), container.getEnvMap().get("MODE"));
    }

    try (LocalS3Container container = new LocalS3Container("latest")) {
      assertEquals(0, container.getPort());
      container.withRandomHttpPort();
      assertNotEquals(0, container.getPort());
      assertEquals(1, container.getPortBindings().size());
      assertTrue(container.getPortBindings().contains(container.getPort() + ":80/tcp"));
    }

  }

}