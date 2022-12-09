package com.robothy.s3.core.asserionts;

import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.exception.BucketNotEmptyException;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.exception.BucketPolicyNotExistException;
import com.robothy.s3.core.exception.BucketReplicationNotExistException;
import com.robothy.s3.core.exception.BucketTaggingNotExistException;
import com.robothy.s3.core.exception.InvalidBucketNameException;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Bucket related assertions.
 */
public class BucketAssertions {

  /**
   * A valid bucket name shouldn't be blank.
   *
   * @param bucketName bucket name to validate.
   * @return valid bucket name.
   */
  public static String assertBucketNameIsValid(String bucketName) {
    if (StringUtils.isBlank(bucketName)) {
      throw new InvalidBucketNameException(bucketName);
    }
    return bucketName;
  }

  /**
   * Assert that the bucket is exists in the {@linkplain LocalS3Metadata}.
   *
   * @param s3Metadata LocalS3 metadata.
   * @param bucketName bucket to validate.
   * @return fetched {@linkplain BucketMetadata} instance.
   */
  public static BucketMetadata assertBucketExists(LocalS3Metadata s3Metadata, String bucketName) {
    return s3Metadata.getBucketMetadata(bucketName)
        .orElseThrow(() -> new BucketNotExistException(bucketName));
  }

  /**
   * Assert that the bucket not exist in the {@code s3Metadata}.
   *
   * @param s3Metadata LocalS3 metadata.
   * @param bucketName bucket to validate.
   */
  public static void assertBucketNotExists(LocalS3Metadata s3Metadata, String bucketName) {
    if (s3Metadata.getBucketMetadataMap().containsKey(bucketName)) {
      throw new BucketAlreadyExistsException(bucketName);
    }
  }

  /**
   * Assert that the bucket is empty.
   *
   * @param bucketMetadata bucket metadata.
   */
  public static void assertBucketIsEmpty(BucketMetadata bucketMetadata) {
    if (!bucketMetadata.getObjectMap().isEmpty()) {
      throw new BucketNotEmptyException(bucketMetadata.getBucketName());
    }
  }

  /**
   * Assert the bucket tagging is set.
   *
   * @param bucketMetadata bucket metadata.
   * @return bucket tagging of the specified bucket metadata.
   */
  public static Collection<Map<String, String>> assertBucketTaggingExist(BucketMetadata bucketMetadata) {
    return bucketMetadata.getTagging()
        .orElseThrow(() -> new BucketTaggingNotExistException(bucketMetadata.getBucketName()));
  }

  /**
   * Assert policy of the specified bucket exist.
   */
  public static String assertBucketPolicyExist(LocalS3Metadata s3Metadata, String bucketName) {
    BucketMetadata bucketMetadata = assertBucketExists(s3Metadata, bucketName);
    return assertBucketPolicyExist(bucketMetadata);
  }

  /**
   * Assert the given {@linkplain BucketMetadata} has policy.
   */
  public static String assertBucketPolicyExist(BucketMetadata bucketMetadata) {
    return bucketMetadata.getPolicy().orElseThrow(() -> new BucketPolicyNotExistException(bucketMetadata.getBucketName()));
  }

  /**
   * Assert the specified bucket has configured replication.
   *
   * @param s3Metadata LocalS3 metadata.
   * @param bucketName the bucket name.
   * @return bucket replication configuration.
   */
  public static String assertBucketReplicationExist(LocalS3Metadata s3Metadata, String bucketName) {
    BucketMetadata bucketMetadata = assertBucketExists(s3Metadata, bucketName);
    return bucketMetadata.getReplication().orElseThrow(BucketReplicationNotExistException::new);
  }

}
