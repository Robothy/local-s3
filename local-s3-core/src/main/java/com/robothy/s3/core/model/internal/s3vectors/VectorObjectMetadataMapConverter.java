package com.robothy.s3.core.model.internal.s3vectors;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Jackson converter for deserializing VectorObjectMetadata maps.
 * Ensures thread-safe concurrent maps are used for vector object metadata storage.
 */
public class VectorObjectMetadataMapConverter extends StdConverter<ConcurrentSkipListMap<String, VectorObjectMetadata>, ConcurrentSkipListMap<String, VectorObjectMetadata>> {

  @Override
  public ConcurrentSkipListMap<String, VectorObjectMetadata> convert(ConcurrentSkipListMap<String, VectorObjectMetadata> value) {
    if (value == null) {
      return new ConcurrentSkipListMap<>();
    }
    // Ensure we have a proper ConcurrentSkipListMap instance
    if (value instanceof ConcurrentSkipListMap) {
      return value;
    }
    // Convert to ConcurrentSkipListMap if it's a different map type
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = new ConcurrentSkipListMap<>();
    result.putAll(value);
    return result;
  }
}
