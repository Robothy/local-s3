package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.InMemoryBucketService;
import com.robothy.s3.core.service.InMemoryObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.loader.FileSystemS3MetadataLoader;
import com.robothy.s3.core.storage.Storage;
import com.robothy.s3.core.util.JsonUtils;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In memory implementation of {@linkplain LocalS3Manager}. Mange in memory
 * local-s3 related services.
 */
final class InMemoryLocalS3Manager implements LocalS3Manager {

  private final LocalS3Metadata s3Metadata;

  private final Storage storage;

  private static final InitialDataCache cache = new InitialDataCache();

  /**
   * Create a {@linkplain InMemoryLocalS3Manager} with initial data.
   * @param initialDataPath initial data path.
   */
  InMemoryLocalS3Manager(Path initialDataPath, boolean enableInitialDataCache) {
    if (Objects.isNull(initialDataPath) || !Files.exists(initialDataPath)) {
      this.storage = Storage.createInMemory();
      this.s3Metadata = new LocalS3Metadata();
    } else {

      String absPath = initialDataPath.toAbsolutePath().toString();
      Path storagePath = Paths.get(initialDataPath.toAbsolutePath().toString(), STORAGE_DIRECTORY);
      if (enableInitialDataCache) {
        if(cache.get(absPath).isEmpty()) {
          synchronized(cache) {
            if (cache.get(absPath).isEmpty()) {
              LocalS3Metadata metadata = loadS3Metadata(initialDataPath);
              Storage persistent = Storage.createPersistent(storagePath);
              // Create a CopyOnAccessStorage for the persistent one to reduce disk I/O.
              Storage copyOnAccess = Storage.createCopyOnAccess(persistent);
              InitialDataCache.CacheValue cacheValue = new InitialDataCache.CacheValue(metadata, copyOnAccess);
              cache.put(absPath, cacheValue);
            }
          }
        }

        InitialDataCache.CacheValue cacheValue = cache.get(absPath).get();
        this.storage = cacheValue.storage();
        this.s3Metadata = cacheValue.metadata();

      } else {
        this.storage = Storage.createLayered(Storage.createInMemory(), Storage.createPersistent(storagePath));
        this.s3Metadata = loadS3Metadata(initialDataPath);
      }

    }
  }

  /**
   * Create an {@linkplain InMemoryLocalS3Manager} with initial data.
   *
   * @param initialMetadata initial metadata.
   * @param initialStorage initial storage.
   */
  InMemoryLocalS3Manager(LocalS3Metadata initialMetadata, Storage initialStorage) {
    this.s3Metadata = Optional.ofNullable(initialMetadata).orElseGet(LocalS3Metadata::new);
    this.storage = Optional.ofNullable(initialStorage).orElseGet(Storage::createInMemory);
  }

  @Override
  public BucketService bucketService() {
    BucketService bucketService = InMemoryBucketService.create(s3Metadata);
    LocalS3ServicesInvocationHandler invocationHandler = new LocalS3ServicesInvocationHandler(bucketService, s3Metadata, null);
    return (BucketService) Proxy.newProxyInstance(BucketService.class.getClassLoader(), new Class[] {BucketService.class}, invocationHandler);
  }

  @Override
  public ObjectService objectService() {
    ObjectService objectService = InMemoryObjectService.create(s3Metadata, storage);
    LocalS3ServicesInvocationHandler invocationHandler = new LocalS3ServicesInvocationHandler(objectService, s3Metadata, null);
    return (ObjectService) Proxy.newProxyInstance(ObjectService.class.getClassLoader(), new Class[] {ObjectService.class}, invocationHandler);
  }

  private LocalS3Metadata loadS3Metadata(Path initialDataDirectory) {
    if (Objects.isNull(initialDataDirectory)) {
      return new LocalS3Metadata();
    }

    if (!Files.exists(initialDataDirectory)) {
      throw new IllegalArgumentException(initialDataDirectory.toAbsolutePath() + " not found.");
    }

    return FileSystemS3MetadataLoader.create().load(initialDataDirectory);
  }

  static class InitialDataCache {

    private final Map<String, CacheValue> cache = new HashMap<>();

    public Optional<CacheValue> get(String key) {
      return Optional.ofNullable(cache.get(key));
    }

    public void put(String key, CacheValue value) {
      this.cache.put(key, value);
    }

    static class CacheValue {

      private final LocalS3Metadata metadata;

      /**
       * This storage should be a {@code CopyOnAccessStorage}.
       */
      private final Storage storage;

      CacheValue(LocalS3Metadata metadata, Storage storage) {
        this.metadata = metadata;
        this.storage = storage;
      }

      /**
       * Create a {@code LayeredStorage} with the real storage as backend and
       * a new {@code InMemoryStorage} as frontend.
       *
       * @return a {@code LayeredStorage} to make sure the real data won't be polluted.
       */
      public Storage storage() {
        return Storage.createLayered(Storage.createInMemory(), storage);
      }

      /**
       * Create a copy of cached data.
       *
       * @return cached metadata.
       */
      public LocalS3Metadata metadata() {
        return JsonUtils.fromJson(JsonUtils.toJson(metadata), LocalS3Metadata.class);
      }
    }

  }

}
