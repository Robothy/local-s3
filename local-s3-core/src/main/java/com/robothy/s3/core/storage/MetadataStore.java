package com.robothy.s3.core.storage;

import java.util.List;

/**
 * Metadata store abstraction.
 *
 * @param <T> the metadata type.
 */
public interface MetadataStore<T> {

  /**
   * Fet metadata from current metadata store.
   *
   * @param name the metadata instance name.
   * @return fetched metadata instance.
   */
  T fetch(String name);

  /**
   * Store a metadata instance. If a metadata with the same name already exist
   * in the store, the existing one will be overridden.
   *
   * @param metadataObject metadata instance to store.
   * @return the name of stored metadata.
   */
  String store(String name, T metadataObject);

  /**
   * Delete a metadata from store by name.
   *
   * @param name metadata instance name.
   * @throws IllegalStateException if the metadata not exist in the store.
   */
  void delete(String name);

  /**
   * Fetch all metadata instances from the store.
   *
   * @return all metadata instances in the store.
   */
  List<T> fetchAll();

}
