package com.robothy.s3.core.storage;

import com.robothy.s3.core.util.IdUtils;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Key-Value based storage abstraction.
 */
public interface Storage {

  /**
   * Create an {@linkplain InMemoryStorage} without total size limitation.
   *
   * @return a {@linkplain Storage} instance.
   */
  static Storage createInMemory() {
    return new InMemoryStorage();
  }

  /**
   * Create an {@linkplain InMemoryStorage} instance with max total size limit.
   *
   * @param maxTotalSize max total size.
   * @return a {@linkplain Storage} instance.
   */
  static Storage createInMemory(int maxTotalSize) {
    return new InMemoryStorage(maxTotalSize);
  }

  /**
   * Create a persistent storage with a specified path. The path
   * will be created if not exists.
   *
   * @param path where data stores in.
   * @return a {@linkplain Storage} instance.
   */
  static Storage createPersistent(Path path) {
    return new LocalFileSystemStorage(path);
  }

  /**
   * Create a {@linkplain LayeredStorage} instance.
   *
   * @param frontend the fronted storage of the created instance.
   * @param backend the backend storage of the created instance.
   * @return a new {@linkplain LayeredStorage} instance.
   */
  static Storage createLayered(Storage frontend, Storage backend) {
    return new LayeredStorage(frontend, backend);
  }

  /**
   * Create a {@linkplain CopyOnAccessStorage} instance.
   *
   * @param base the base storage of the {@linkplain CopyOnAccessStorage}.
   * @return a {@linkplain CopyOnAccessStorage} instance.
   */
  static Storage createCopyOnAccess(Storage base) {
    return new CopyOnAccessStorage(base);
  }

  /**
   * Put binary data to the storage.
   *
   * @param data data.
   * @return object id.
   */
  default Long put(byte[] data) {
    return put(IdUtils.defaultGenerator().nextId(), data);
  }

  /**
   * Put data to the storage.
   *
   * @param id the file ID. Override data if the ID already exist.
   * @param data data.
   * @return the object ID.
   */
  Long put(Long id, byte[] data);

  /**
   * Put data from an {@linkplain InputStream} to the storage.
   *
   * @param data octet-stream.
   * @return the storage generated object ID.
   */
  default Long put(InputStream data) {
    return put(IdUtils.defaultGenerator().nextId(), data);
  }

  /**
   * Put octet-stream to the storage.
   */
  Long put(Long id, InputStream data);

  /**
   * Get all bytes of the object by ID.
   *
   * @param id the object ID.
   * @return fetched data.
   */
  byte[] getBytes(Long id);

  /**
   * Get the {@linkplain InputStream} of the object.
   *
   * @param id the object ID.
   * @return octet-stream.
   */
  InputStream getInputStream(Long id);

  /**
   * Delete an object by ID.
   *
   * @param id the object ID.
   * @return deleted Object ID.
   */
  Long delete(Long id);

  /**
   * Is the object with specified ID exists.
   *
   * @param id object ID.
   * @return {@code true} if the ID exists; otherwise {@code false}.
   */
  boolean isExist(Long id);

}
