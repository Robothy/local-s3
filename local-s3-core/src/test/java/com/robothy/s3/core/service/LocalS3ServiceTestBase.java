package com.robothy.s3.core.service;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.service.manager.LocalS3Manager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Test base for LocalS3 service tests.
 */
public abstract class LocalS3ServiceTestBase {

  /**
   * Use these test cases if the test only relates to {@linkplain BucketService}.
   */
  static Stream<Arguments> bucketServices() throws IOException {
    Path bucketStore = Files.createTempDirectory("bucket-metadata");
    bucketStore.toFile().deleteOnExit();
    return Stream.of(
        arguments(InMemoryBucketService.create(new LocalS3Metadata())),
        arguments(LocalS3Manager.createFileSystemS3Manager(bucketStore).bucketService())
    );
  }

  /**
   * Use these test cases if the test relates to {@linkplain BucketService} and {@linkplain ObjectService}.
   *
   * @return {@linkplain com.robothy.s3.core.service.manager.LocalS3Manager}s.
   */
  static Stream<Arguments> localS3Managers() throws Exception {
    Path localS3Path = Files.createTempDirectory("local-s3");
    localS3Path.toFile().deleteOnExit();
    return Stream.of(
        arguments(LocalS3Manager.createInMemoryS3Manager()),
        arguments(LocalS3Manager.createFileSystemS3Manager(localS3Path))
    );
  }


  static Stream<Arguments> localS3Services() throws Exception {
    Path localS3Path = Files.createTempDirectory("local-s3");
    localS3Path.toFile().deleteOnExit();
    LocalS3Manager inMemoryS3Manager = LocalS3Manager.createInMemoryS3Manager();
    LocalS3Manager fileSystemS3Manager = LocalS3Manager.createFileSystemS3Manager(localS3Path);
    return Stream.of(
        arguments(inMemoryS3Manager.bucketService(), inMemoryS3Manager.objectService()),
        arguments(fileSystemS3Manager.bucketService(), fileSystemS3Manager.objectService())
    );
  }

}
