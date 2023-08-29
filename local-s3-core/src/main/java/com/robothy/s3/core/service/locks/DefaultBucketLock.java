package com.robothy.s3.core.service.locks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class DefaultBucketLock implements BucketLock {

  static final BucketLock singleton = new DefaultBucketLock();

  private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

  @Override
  public Lock readLock(String bucketName) {
    return getLock(bucketName).readLock();
  }

  @Override
  public Lock writeLock(String bucketName) {
    return getLock(bucketName).writeLock();
  }

  private ReadWriteLock getLock(String bucketName) {
    return locks.computeIfAbsent(bucketName, k -> new ReentrantReadWriteLock());
  }

}
