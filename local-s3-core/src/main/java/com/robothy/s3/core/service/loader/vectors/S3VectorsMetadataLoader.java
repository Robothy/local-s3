package com.robothy.s3.core.service.loader.vectors;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.service.loader.MetadataLoader;
import com.robothy.s3.core.storage.MetadataStore;
import com.robothy.s3.core.storage.s3vectors.FileSystemVectorBucketMetadataStore;
import com.robothy.s3.core.util.PathUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class S3VectorsMetadataLoader implements MetadataLoader<LocalS3VectorsMetadata> {

  @Override
  public LocalS3VectorsMetadata load(Path s3DataPath) {
    Objects.requireNonNull(s3DataPath);
    PathUtils.createDirectoryIfNotExit(s3DataPath);
    File versionFile = new File(s3DataPath.toFile(), VERSION_FILE_NAME);
    LocalS3VectorsMetadata s3Metadata = new LocalS3VectorsMetadata();
    if (!versionFile.exists()) {
      try {
        Files.write(versionFile.toPath(), String.valueOf(LocalS3Metadata.VERSION).getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new IllegalStateException("Failed to load S3 metadata from " + s3DataPath);
      }
    }

    MetadataStore<VectorBucketMetadata> bucketMetaStore = FileSystemVectorBucketMetadataStore.create(s3DataPath);
    bucketMetaStore.fetchAll().forEach(s3Metadata::addVectorBucketMetadata);
    return s3Metadata;
  }

}
