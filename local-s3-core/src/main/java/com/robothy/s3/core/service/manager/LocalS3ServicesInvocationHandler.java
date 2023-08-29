package com.robothy.s3.core.service.manager;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.service.locks.BucketLock;
import com.robothy.s3.core.storage.MetadataStore;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


final class LocalS3ServicesInvocationHandler implements InvocationHandler {

  private final Object proxy;

  private final MetadataStore<BucketMetadata> bucketMetaStore;

  private final LocalS3Metadata s3Metadata;

  LocalS3ServicesInvocationHandler(Object proxy, LocalS3Metadata s3Metadata, MetadataStore<BucketMetadata> bucketMetaStore) {
    this.proxy = proxy;
    this.bucketMetaStore = bucketMetaStore;
    this.s3Metadata = s3Metadata;
  }

  @Override
  public Object invoke(Object __, Method method, Object[] args) throws Throwable {
    BucketChanged bucketChanged = method.getDeclaredAnnotation(BucketChanged.class);
    boolean isReadBucket = Objects.nonNull(method.getDeclaredAnnotation(BucketReadLock.class));
    boolean isWriteBucket = Objects.nonNull(method.getDeclaredAnnotation(BucketWriteLock.class));

    lockIfNeeded(args, isReadBucket, isWriteBucket);
    try {
      Object result = method.invoke(proxy, args);
      persistBucketIfNeeded(args, bucketChanged);
      return result;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } finally {
      unlockIfNeeded(args, isReadBucket, isWriteBucket);
    }
  }

  void lockIfNeeded(Object[] args, boolean isRead, boolean isWrite) {
    if (isRead || isWrite) {
      String bucketName = (String) args[0];
      BucketLock lock = BucketLock.getInstance();
      if (isRead) {
        lock.readLock(bucketName).lock();
      }

      if (isWrite) {
        lock.writeLock(bucketName).lock();
      }
    }
  }

  void unlockIfNeeded(Object[] args, boolean isRead, boolean isWrite) {
    String bucketName = (String) args[0];
    if (isRead || isWrite) {
      BucketLock lock = BucketLock.getInstance();
      if (isRead) {
        lock.readLock(bucketName).unlock();
      }

      if (isWrite) {
        lock.writeLock(bucketName).unlock();
      }
    }
  }

  void persistBucketIfNeeded(Object[] args, BucketChanged bucketChanged) {
    if (Objects.isNull(bucketChanged) || Objects.isNull(bucketMetaStore)) {
      return;
    }

    String bucketName = (String) args[0];
    switch (bucketChanged.type()) {
      case UPDATE:
      case CREATE:
        bucketMetaStore.store(bucketName, s3Metadata.getBucketMetadata(bucketName).get());
        break;
      case DELETE:
        bucketMetaStore.delete(bucketName);
        break;
    }
  }


}
