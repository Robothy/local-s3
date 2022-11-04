package com.robothy.s3.core.storage;

import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.util.JsonUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class FileSystemBucketMetadataStore implements MetadataStore<BucketMetadata> {

  private static final String BUCKET_METADATA_FILE_SUFFIX = ".bucket.meta";

  public static MetadataStore<BucketMetadata> create(Path dataPath) {
    Objects.requireNonNull(dataPath);
    File file = dataPath.toFile();
    if (!file.exists() || !file.isDirectory()) {
      if (!file.mkdirs()) {
        throw new IllegalStateException("Failed to create directory " + dataPath.toAbsolutePath());
      }
    }
    return new FileSystemBucketMetadataStore(dataPath);
  }

  private final Path dataPath;

  private FileSystemBucketMetadataStore(Path path) {
    this.dataPath = path;
  }

  @SneakyThrows
  @Override
  public BucketMetadata fetch(String bucketName) {
    return JsonUtils.fromJson(new File(dataPath.toFile(), bucketName + BUCKET_METADATA_FILE_SUFFIX), BucketMetadata.class);
  }

  @Override
  public String store(String bucketName, BucketMetadata bucketMetadata) {
    if (StringUtils.isBlank(bucketMetadata.getBucketName())) {
      throw new IllegalArgumentException("Invalid bucket name '" + bucketMetadata.getBucketName() + "'.");
    }

    JsonUtils.toJson(new File(dataPath.toFile(), bucketMetadata.getBucketName() + BUCKET_METADATA_FILE_SUFFIX), bucketMetadata);
    return bucketMetadata.getBucketName();
  }

  @Override
  public void delete(String bucketName) {
    if (!new File(dataPath.toFile(), bucketName + BUCKET_METADATA_FILE_SUFFIX).delete()) {
      throw new IllegalStateException("Failed to delete metadata of bucket " + bucketName);
    }
  }

  @Override
  @SneakyThrows
  public List<BucketMetadata> fetchAll() {
    try (Stream<Path> pathStream = Files.walk(dataPath, 1)) {
      return pathStream
          .filter(path -> path.toString().endsWith(BUCKET_METADATA_FILE_SUFFIX))
          .map(path -> path.getFileName().toString())
          .map(fileName -> fileName.substring(0, fileName.lastIndexOf(BUCKET_METADATA_FILE_SUFFIX)))
          .map(this::fetch)
          .collect(Collectors.toList());
    }
  }
}
