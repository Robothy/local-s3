package com.robothy.s3.core.storage;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StorageOptions {

  /**
   * 512 MB
   */
  public static final long _512MB = 512 * 1024 * 1024;

  /**
   * Unlimited size.
   */
  public static final long UNLIMITED_SIZE = Long.MAX_VALUE;

  /**
   * If data stores in Java Heap. Default is {@code true}.
   */
  @Builder.Default
  private boolean inMemory = true;

  /**
   * The directory that stores objects. Required for {@linkplain LocalFileSystemStorage}.
   */
  private Path directory;

  /**
   * The maximum total size of objects in this collection. Only available for {@linkplain InMemoryStorage}.
   * Default is unlimited size.
   */
  @Builder.Default
  private long maxTotalSize = UNLIMITED_SIZE;

}
