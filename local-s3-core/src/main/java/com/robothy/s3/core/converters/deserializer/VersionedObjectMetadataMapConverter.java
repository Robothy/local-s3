package com.robothy.s3.core.converters.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Convert {@linkplain ObjectMetadata#getVersionedObjectMap()} to a {@linkplain java.util.concurrent.ConcurrentHashMap} instance.
 */
public class VersionedObjectMetadataMapConverter extends StdConverter<Map<String, VersionedObjectMetadata>, ConcurrentSkipListMap<String, VersionedObjectMetadata>> {

  @Override
  public ConcurrentSkipListMap<String, VersionedObjectMetadata> convert(Map<String, VersionedObjectMetadata> value) {
    ConcurrentSkipListMap<String, VersionedObjectMetadata> result = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    result.putAll(value);
    return result;
  }
}
