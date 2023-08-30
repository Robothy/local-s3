package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.InMemoryBucketService;
import com.robothy.s3.core.service.InMemoryObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.loader.FileSystemS3MetadataLoader;
import com.robothy.s3.core.storage.FileSystemBucketMetadataStore;
import com.robothy.s3.core.storage.MetadataStore;
import com.robothy.s3.core.storage.Storage;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

final class FileSystemLocalS3Manager implements LocalS3Manager {

  private final LocalS3Metadata s3Metadata;

  private final MetadataStore<BucketMetadata> bucketMetaStore;

  private final Storage storage;

  FileSystemLocalS3Manager(Path dataDirectory) {
    Objects.requireNonNull(dataDirectory, "Data directory is required to create a persistent LocalS3 service.");
    this.bucketMetaStore = FileSystemBucketMetadataStore.create(dataDirectory);
    this.s3Metadata = FileSystemS3MetadataLoader.create().load(dataDirectory);
    this.storage = Storage.createPersistent(Paths.get(dataDirectory.toAbsolutePath().toString(), STORAGE_DIRECTORY));
  }

  @Override
  public BucketService bucketService() {
    BucketService delegated = InMemoryBucketService.create(s3Metadata);
    LocalS3ServicesInvocationHandler invocationHandler = new LocalS3ServicesInvocationHandler(delegated, s3Metadata, bucketMetaStore);
    return (BucketService) Proxy.newProxyInstance(BucketService.class.getClassLoader(), new Class[] {BucketService.class}, invocationHandler);
  }

  @Override
  public ObjectService objectService() {
    ObjectService delegated = InMemoryObjectService.create(s3Metadata, storage);
    LocalS3ServicesInvocationHandler invocationHandler = new LocalS3ServicesInvocationHandler(delegated, s3Metadata, bucketMetaStore);
    return (ObjectService) Proxy.newProxyInstance(ObjectService.class.getClassLoader(), new Class[] {ObjectService.class}, invocationHandler);
  }

}
