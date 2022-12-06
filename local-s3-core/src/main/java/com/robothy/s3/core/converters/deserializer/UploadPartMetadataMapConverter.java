package com.robothy.s3.core.converters.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Convert upload part metadata map to a {@linkplain java.util.concurrent.ConcurrentSkipListMap} instance.
 */
public class UploadPartMetadataMapConverter extends StdConverter<Map<String, UploadPartMetadata>,
    ConcurrentSkipListMap<Integer, UploadPartMetadata>> {

  @Override
  public ConcurrentSkipListMap<Integer, UploadPartMetadata> convert(Map<String, UploadPartMetadata> value) {
    ConcurrentSkipListMap<Integer, UploadPartMetadata> uploads = new ConcurrentSkipListMap<>();
    value.forEach((k, v) -> uploads.put(Integer.valueOf(k), v));
    return uploads;
  }

}
