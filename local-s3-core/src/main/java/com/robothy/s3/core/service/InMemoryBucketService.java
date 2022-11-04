package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.model.S3Object;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import java.util.List;
import java.util.Objects;

/**
 * In memory implementation of {@linkplain BucketService}. All related data
 * are stored in memory.
 */
public class InMemoryBucketService implements BucketService {

  /**
   * Create an {@linkplain InMemoryBucketService} with a {@linkplain LocalS3Metadata} instance.
   *
   * @param s3Metadata s3 metadata.
   * @return a new {@linkplain InMemoryBucketService} with a {@linkplain BucketMetadata} from the {@code provider}.
   */
  public static BucketService create(LocalS3Metadata s3Metadata) {
    Objects.requireNonNull(s3Metadata);
    return new InMemoryBucketService(s3Metadata);
  }

  private final LocalS3Metadata s3Metadata;

  private InMemoryBucketService(LocalS3Metadata metadata) {
    this.s3Metadata = metadata;
  }

  @Override
  public LocalS3Metadata localS3Metadata() {
    return this.s3Metadata;
  }

  @Override
  public Bucket createBucket(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketAssertions.assertBucketNotExists(s3Metadata, bucketName);
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName(bucketName);
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    s3Metadata.addBucketMetadata(bucketMetadata);
    return Bucket.fromBucketMetadata(bucketMetadata);
  }

  @Override
  public Bucket deleteBucket(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    BucketAssertions.assertBucketIsEmpty(bucketMetadata);
    s3Metadata.getBucketMetadataMap().remove(bucketName);
    return Bucket.fromBucketMetadata(bucketMetadata);
  }

  @Override
  public Bucket getBucket(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    return Bucket.fromBucketMetadata(bucketMetadata);
  }

  @Override
  public List<S3Object> listObjects(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    throw new UnsupportedOperationException();
  }

  @Override
  public Bucket setVersioningEnabled(String bucketName, boolean versioningEnabled) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    bucketMetadata.setVersioningEnabled(versioningEnabled);
    return Bucket.fromBucketMetadata(bucketMetadata);
  }

  @Override
  public Boolean getVersioningEnabled(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucketName);
    return bucketMetadata.getVersioningEnabled();
  }

  @Override
  public void putSetting(String bucketName, String key, String value) {

  }

  @Override
  public String getSetting(String bucketName, String key) {
    return null;
  }
}
