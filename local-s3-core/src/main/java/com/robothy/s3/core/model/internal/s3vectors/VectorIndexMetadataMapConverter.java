package com.robothy.s3.core.model.internal.s3vectors;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Jackson converter for deserializing VectorIndexMetadata maps.
 * Ensures thread-safe concurrent maps are used for vector index metadata storage.
 */
public class VectorIndexMetadataMapConverter extends StdConverter<ConcurrentSkipListMap<String, VectorIndexMetadata>, ConcurrentSkipListMap<String, VectorIndexMetadata>> {

  @Override
  public ConcurrentSkipListMap<String, VectorIndexMetadata> convert(ConcurrentSkipListMap<String, VectorIndexMetadata> value) {
    if (value == null) {
      return new ConcurrentSkipListMap<>();
    }
    return value;
  }
}
