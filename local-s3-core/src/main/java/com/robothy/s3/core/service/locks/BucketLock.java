package com.robothy.s3.core.service.locks;

import java.util.concurrent.locks.Lock;

public interface BucketLock {

  static BucketLock getInstance() {
    return DefaultBucketLock.singleton;
  }

  Lock readLock(String bucketName);

  Lock writeLock(String bucketName);

}
