package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.annotations.BucketChanged;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileSystemLocalS3Proxy implements LocalS3Manager {

  private final LocalS3Metadata s3Metadata;

  private final MetadataStore<BucketMetadata> bucketMetaStore;

  private final Storage storage;

  FileSystemLocalS3Proxy(Path dataDirectory) {
    Objects.requireNonNull(dataDirectory, "Data directory is required to create a persistent LocalS3 service.");
    this.bucketMetaStore = FileSystemBucketMetadataStore.create(dataDirectory);
    this.s3Metadata = FileSystemS3MetadataLoader.create().load(dataDirectory);
    this.storage = Storage.createPersistent(Paths.get(dataDirectory.toAbsolutePath().toString(), STORAGE_DIRECTORY));
  }

  @Override
  public BucketService bucketService() {
    BucketService proxy = InMemoryBucketService.create(s3Metadata);
    InvocationHandler handler = new LocalS3ServiceInvocationHandler(proxy, s3Metadata, bucketMetaStore);
    ClassLoader classLoader = BucketService.class.getClassLoader();
    return (BucketService) Proxy.newProxyInstance(classLoader, new Class[] {BucketService.class}, handler);
  }

  @Override
  public ObjectService objectService() {
    ObjectService proxy = InMemoryObjectService.create(s3Metadata, storage);
    LocalS3ServiceInvocationHandler handler = new LocalS3ServiceInvocationHandler(proxy, s3Metadata, bucketMetaStore);
    ClassLoader classLoader = ObjectService.class.getClassLoader();
    return (ObjectService) Proxy.newProxyInstance(classLoader, new Class[] {ObjectService.class}, handler);
  }

  private static class LocalS3ServiceInvocationHandler implements InvocationHandler {

    private final Object proxy;

    private final MetadataStore<BucketMetadata> bucketMetaStore;

    private final LocalS3Metadata s3Metadata;

    LocalS3ServiceInvocationHandler(Object proxy, LocalS3Metadata s3Metadata, MetadataStore<BucketMetadata> bucketMetaStore) {
      this.proxy = proxy;
      this.bucketMetaStore = bucketMetaStore;
      this.s3Metadata = s3Metadata;
    }

    @Override
    public Object invoke(Object __, Method method, Object[] args) throws Throwable {
      Object result;
      try {
        result = method.invoke(this.proxy, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
      BucketChanged bucketChanged = method.getDeclaredAnnotation(BucketChanged.class);
      if (Objects.nonNull(bucketChanged)) {
        String bucketName = (String) args[0];
        switch (bucketChanged.type()) {
          case UPDATE :
          case CREATE :
            bucketMetaStore.store(bucketName, s3Metadata.getBucketMetadata(bucketName).get());
          break;
          case DELETE : bucketMetaStore.delete(bucketName);
        }
      }

      return result;
    }

  }

}
