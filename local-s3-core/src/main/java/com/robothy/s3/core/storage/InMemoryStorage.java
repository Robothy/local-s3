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

  private final StorageOptions options;

  /**
   * Construct an instance with options.
   *
   * @param options options.
   */
  public InMemoryStorage(StorageOptions options) {
    this.options = options;
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

  private void ensureObjectExist(Long id) {
    if (!store.containsKey(id)) {
      throw new IllegalArgumentException("Object id='" + id + "' not exist.");
    }
  }

  private void ensureNotExceedTotalSize(int incrementalSize) {
    if (totalSize.get() + incrementalSize > options.getMaxTotalSize()) {
      throw new TotalSizeExceedException(options.getMaxTotalSize(), totalSize.get() + incrementalSize);
    }
  }

}
