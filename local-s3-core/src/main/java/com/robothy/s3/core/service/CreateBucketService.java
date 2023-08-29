package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.model.internal.BucketMetadata;

/**
 * Create bucket service.
 * <p>
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateBucket.html">CreateBucket</a>
 */
public interface CreateBucketService extends LocalS3MetadataApplicable {

  /**
   * Create a bucket.
   */
  @BucketChanged(type = BucketChanged.Type.CREATE)
  @BucketWriteLock
  default Bucket createBucket(String bucketName) {
    return createBucket(bucketName, null);
  }

  /**
   * Create a bucket.
   */
  @BucketChanged(type = BucketChanged.Type.CREATE)
  @BucketWriteLock
  default Bucket createBucket(String bucketName, String region) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketAssertions.assertBucketNotExists(localS3Metadata(), bucketName);
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName(bucketName);
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    bucketMetadata.setRegion(region);
    localS3Metadata().addBucketMetadata(bucketMetadata);
    return Bucket.fromBucketMetadata(bucketMetadata);
  }

}
