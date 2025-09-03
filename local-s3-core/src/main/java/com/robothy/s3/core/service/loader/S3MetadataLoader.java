package com.robothy.s3.core.service.loader;

import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.storage.FileSystemBucketMetadataStore;
import com.robothy.s3.core.storage.MetadataStore;
import com.robothy.s3.core.util.PathUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class S3MetadataLoader implements MetadataLoader<LocalS3Metadata> {

  @Override
  public LocalS3Metadata load(Path s3DataPath) {
    Objects.requireNonNull(s3DataPath);
    PathUtils.createDirectoryIfNotExit(s3DataPath);
    File versionFile = new File(s3DataPath.toFile(), VERSION_FILE_NAME);
    LocalS3Metadata s3Metadata = new LocalS3Metadata();
    if (!versionFile.exists()) {
      try {
        Files.write(versionFile.toPath(), String.valueOf(LocalS3Metadata.VERSION).getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new IllegalStateException("Failed to load S3 metadata from " + s3DataPath);
      }
    }

    MetadataStore<BucketMetadata> bucketMetaStore = FileSystemBucketMetadataStore.create(s3DataPath);
    bucketMetaStore.fetchAll().forEach(s3Metadata::addBucketMetadata);
    return s3Metadata;
  }

}
