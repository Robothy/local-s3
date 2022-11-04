package com.robothy.s3.core.converters.deserializer;

import com.robothy.s3.core.converters.MapToConcurrentSkipListMap;
import com.robothy.s3.core.model.internal.ObjectMetadata;

/**
 * Convert object metadata map to a {@linkplain java.util.concurrent.ConcurrentSkipListMap} instance.
 */
public class ObjectMetadataMapConverter extends MapToConcurrentSkipListMap<String, ObjectMetadata> {
}
