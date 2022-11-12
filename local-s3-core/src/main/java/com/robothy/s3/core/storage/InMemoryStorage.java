package com.robothy.s3.core.storage;

import com.robothy.s3.core.exception.TotalSizeExceedException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;

/**
 * {@linkplain Storage} implementation based on Java Heap.
 */
class InMemoryStorage implements Storage {

  private final Map<Long, byte[]> store = new ConcurrentHashMap<>();

  private final AtomicLong totalSize = new AtomicLong(0);

  private final AtomicLong maxTotalSize = new AtomicLong(Long.MAX_VALUE);

  /**
   * Create an {@linkplain InMemoryStorage} instance with total size limitation.
   *
   * @param maxTotalSize max total size.
   */
  InMemoryStorage(long maxTotalSize) {
    this.maxTotalSize.set(maxTotalSize);
  }

  /**
   * Create an {@linkplain InMemoryStorage} instance without total size limitation.
   */
  InMemoryStorage() {

  }

  @Override
  public Long put(Long id, byte[] data) {
    ensureNotExceedTotalSize(data.length);
    byte[] copy = Arrays.copyOf(data, data.length);
    store.put(id, copy);
    totalSize.addAndGet(data.length);
    return id;
  }

  @Override
  @SneakyThrows
  public Long put(Long id, InputStream data) {
    return put(id, data.readAllBytes());
  }

  @Override
  public byte[] getBytes(Long id) {
    ensureObjectExist(id);
    byte[] data = store.get(id);
    return Arrays.copyOf(data, data.length);
  }

  @Override
  public InputStream getInputStream(Long id) {
    byte[] data = getBytes(id);
    return new ByteArrayInputStream(data);
  }

  @Override
  public Long delete(Long id) {
    ensureObjectExist(id);
    int size = store.get(id).length;
    store.remove(id);
    totalSize.addAndGet(-size);
    return id;
  }

  @Override
  public boolean isExist(Long id) {
    return store.containsKey(id);
  }

  private void ensureObjectExist(Long id) {
    if (!isExist(id)) {
      throw new IllegalArgumentException("Object id='" + id + "' not exists.");
    }
  }

  private void ensureNotExceedTotalSize(int incrementalSize) {
    if (totalSize.get() + incrementalSize > maxTotalSize.get()) {
      throw new TotalSizeExceedException(maxTotalSize.get(), totalSize.get() + incrementalSize);
    }
  }

}
