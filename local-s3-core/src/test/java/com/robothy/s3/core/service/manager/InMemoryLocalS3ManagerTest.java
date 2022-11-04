package com.robothy.s3.core.service.manager;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.robothy.s3.core.service.InMemoryBucketService;
import com.robothy.s3.core.service.InMemoryObjectService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class InMemoryLocalS3ManagerTest {

  @Test
  void testConstructor() throws IOException {
    InMemoryLocalS3Manager managerWithoutInitData = new InMemoryLocalS3Manager(null);
    assertInstanceOf(InMemoryBucketService.class, managerWithoutInitData.bucketService());
    assertInstanceOf(InMemoryObjectService.class, managerWithoutInitData.objectService());

    Path tempDirectory = Files.createTempDirectory("local-s3");
    InMemoryLocalS3Manager managerWithInitData = new InMemoryLocalS3Manager(tempDirectory);
    assertInstanceOf(InMemoryBucketService.class, managerWithInitData.bucketService());
    assertInstanceOf(InMemoryObjectService.class, managerWithInitData.objectService());

  }

}