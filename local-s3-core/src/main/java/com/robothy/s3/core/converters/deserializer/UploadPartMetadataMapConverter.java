package com.robothy.s3.core.converters.deserializer;

import com.robothy.s3.core.converters.MapToConcurrentSkipListMap;
import com.robothy.s3.core.model.internal.UploadPartMetadata;

/**
 * Convert upload part metadata map to a {@linkplain java.util.concurrent.ConcurrentSkipListMap} instance.
 */
public class UploadPartMetadataMapConverter extends MapToConcurrentSkipListMap<String, UploadPartMetadata> {
}
