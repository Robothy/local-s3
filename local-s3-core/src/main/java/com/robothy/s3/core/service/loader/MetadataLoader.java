package com.robothy.s3.core.service.loader;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.service.loader.vectors.S3VectorsMetadataLoader;
import java.nio.file.Path;

public interface MetadataLoader<T> {

  String VERSION_FILE_NAME = "version";

  static <T> MetadataLoader<T> create(Class<T> metadataClazz) {
    if (metadataClazz.equals(LocalS3Metadata.class)) {
      return (MetadataLoader<T>) new S3MetadataLoader();
    } else if (metadataClazz.equals(LocalS3VectorsMetadata.class)) {
      return (MetadataLoader<T>) new S3VectorsMetadataLoader();
    }

    throw new IllegalArgumentException("Unsupported metadata class: " + metadataClazz);
  }

  T load(Path metadataPath);

}
