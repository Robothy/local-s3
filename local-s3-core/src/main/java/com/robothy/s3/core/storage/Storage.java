package com.robothy.s3.core.storage;

import com.robothy.s3.core.util.IdUtils;
import java.io.InputStream;

/**
 * Key-Value based storage abstraction.
 */
public interface Storage {

  static Storage create(StorageOptions options) {
    return options.isInMemory() ? new InMemoryStorage(options)
        : new LocalFileSystemStorage(options);
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

}
