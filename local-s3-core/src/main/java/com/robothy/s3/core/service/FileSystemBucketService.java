package com.robothy.s3.core.service;

import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.model.S3Object;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.storage.MetadataStore;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;

/**
 * File system implementation of {@linkplain BucketService}.
 * All related bucket changes will be persisted in the file system.
 */
@Deprecated
public class FileSystemBucketService implements BucketService {

  /**
   * Create a {@linkplain FileSystemBucketService}.
   *
   * @param s3Metadata the LocalS3 metadata.
   * @param bucketMetaStore bucket metadata store service.
   * @return a new {@linkplain BucketService}.
   */
  public static BucketService create(LocalS3Metadata s3Metadata, MetadataStore<BucketMetadata> bucketMetaStore) {
    return new FileSystemBucketService(s3Metadata, bucketMetaStore);
  }

  private final BucketService inMemoService;

  private final LocalS3Metadata s3Metadata;

  private final MetadataStore<BucketMetadata> bucketMetaStore;

  private FileSystemBucketService(LocalS3Metadata s3Metadata, MetadataStore<BucketMetadata> bucketMetaStore) {
    this.s3Metadata = s3Metadata;
    this.bucketMetaStore = bucketMetaStore;
    this.inMemoService = InMemoryBucketService.create(s3Metadata);
  }

  @Override
  public LocalS3Metadata localS3Metadata() {
    return this.s3Metadata;
  }

  @Override
  public Bucket createBucket(String bucketName) {
    Bucket bucket = this.inMemoService.createBucket(bucketName);
    Optional<BucketMetadata> bucketMetadataOpt = this.s3Metadata.getBucketMetadata(bucketName);
    if (bucketMetadataOpt.isEmpty()) {
      throw new IllegalStateException("Failed to create bucket " + bucketName);
    }
    BucketMetadata bucketMetadata = bucketMetadataOpt.get();
    bucketMetaStore.store(bucketName, bucketMetadata);
    return bucket;
  }

  @Override
  @SneakyThrows
  public Bucket deleteBucket(String bucketName) {
    Bucket bucket = this.inMemoService.deleteBucket(bucketName);
    bucketMetaStore.delete(bucketName);
    return bucket;
  }

  @Override
  public Bucket getBucket(String bucketName) {
    return this.inMemoService.getBucket(bucketName);
  }

  @Override
  public List<S3Object> listObjects(String bucketName) {
    return this.inMemoService.listObjects(bucketName);
  }

  @Override
  public Bucket setVersioningEnabled(String bucketName, boolean versioningEnabled) {
    Bucket bucket = inMemoService.setVersioningEnabled(bucketName, versioningEnabled);
    bucketMetaStore.store(bucketName, s3Metadata.getBucketMetadata(bucketName).get());
    return bucket;
  }

  @Override
  public Boolean getVersioningEnabled(String bucketName) {
    return inMemoService.getVersioningEnabled(bucketName);
  }

  @Override
  public void putSetting(String bucketName, String key, String value) {

  }

  @Override
  public String getSetting(String bucketName, String key) {
    return null;
  }
}
