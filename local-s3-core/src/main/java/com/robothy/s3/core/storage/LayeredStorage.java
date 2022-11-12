package com.robothy.s3.core.storage;

import java.io.InputStream;
import java.util.Objects;

/**
 * A {@linkplain LayeredStorage} has a backend and frontend {@linkplain Storage}.
 * When writing to a {@linkplain LayeredStorage}, data stores in the frontend storage.
 * When reading from a {@linkplain LayeredStorage}, it firstly access the frontend,
 * then read from the backend; otherwise throw {@linkplain java.io.FileNotFoundException}.
 * When deleting an object from {@linkplain LayeredStorage}, the operation only performed
 * on the frontend storage.
 */
class LayeredStorage implements Storage {

  private final Storage back;

  private final Storage front;

  /**
   * Construct a {@linkplain LayeredStorage} instance.
   *
   * @param frontend the front storage.
   * @param backend the back of current storage instance.
   */
  public LayeredStorage(Storage frontend, Storage backend) {
    Objects.requireNonNull(frontend);
    Objects.requireNonNull(backend);
    this.front = frontend;
    this.back = backend;
  }

  @Override
  public Long put(Long id, byte[] data) {
    return this.front.put(id, data);
  }

  @Override
  public Long put(Long id, InputStream data) {
    return this.front.put(id, data);
  }

  @Override
  public byte[] getBytes(Long id) {
    return front.isExist(id) ? front.getBytes(id) : back.getBytes(id);
  }

  @Override
  public InputStream getInputStream(Long id) {
    return front.isExist(id) ? front.getInputStream(id) : back.getInputStream(id);
  }

  /**
   * Delete the object ID from the front storage if exists.
   *
   * @param id the object ID.
   * @return delete object ID.
   */
  @Override
  public Long delete(Long id) {
    if (front.isExist(id)) {
      return front.delete(id);
    }

    if (!back.isExist(id)) {
      back.delete(id); // trigger throwing an exception.
    }

    return id;
  }

  @Override
  public boolean isExist(Long id) {
    return front.isExist(id) || back.isExist(id);
  }

}
