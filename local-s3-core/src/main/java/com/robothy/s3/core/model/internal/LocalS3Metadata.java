package com.robothy.s3.core.model.internal;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents medata of a local-s3 service. The instance could be very large.
 * Therefore, one JVM usually has one {@linkplain LocalS3Metadata} instance.
 */
public class LocalS3Metadata {

  /**
   * Represents the version of {@linkplain LocalS3Metadata} model.
   */
  public static final long VERSION = 1L;

  private static final Comparator<BucketMetadata> DEFAULT_BUCKET_METADATA_COMPARATOR = Comparator
      .comparing(BucketMetadata::getCreationDate);
  
  private final Map<String, BucketMetadata> bucketMetadataMap = new ConcurrentHashMap<>();
  private final LocalS3VectorsMetadata vectorsMetadata = new LocalS3VectorsMetadata();

  /**
   * List ordered buckets with a comparator.
   *
   * @param comparator that determine the returned result order.
   * @return sorted bucket metadata.
   */
  public List<BucketMetadata> listBuckets(Comparator<BucketMetadata> comparator) {
    ArrayList<BucketMetadata> bucketList = new ArrayList<>(bucketMetadataMap.values());
    bucketList.sort(comparator);
    return bucketList;
  }

  /**
   * List buckets order by creation date.
   *
   * @return all buckets of current {@linkplain LocalS3Metadata}.
   */
  public List<BucketMetadata> listBuckets() {
    return listBuckets(DEFAULT_BUCKET_METADATA_COMPARATOR);
  }

  /**
   * Get bucket metadata map of current {@linkplain LocalS3Metadata} instance.
   * The key represents the bucket name, while the value represents the
   * bucket metadata.
   *
   * @return a map of bucket metadata.
   */
  public Map<String, BucketMetadata> getBucketMetadataMap() {
    return bucketMetadataMap;
  }

  /**
   * Get the vectors metadata instance.
   *
   * @return the vectors metadata instance.
   */
  public LocalS3VectorsMetadata getVectorsMetadata() {
    return vectorsMetadata;
  }

  /**
   * Get the metadata of {@code bucketName}.
   *
   * @param bucketName the bucket name.
   * @return bucket metadata.
   */
  public Optional<BucketMetadata> getBucketMetadata(String bucketName) {
    return Optional.ofNullable(bucketMetadataMap.get(bucketName));
  }

  /**
   * Add a {@linkplain BucketMetadata} instance.
   *
   * @param bucketMetadata bucket metadata.
   * @throws BucketAlreadyExistsException if the bucket name already used.
   */
  public void addBucketMetadata(BucketMetadata bucketMetadata) {
    BucketAssertions.assertBucketNotExists(this, bucketMetadata.getBucketName());
    this.bucketMetadataMap.put(bucketMetadata.getBucketName(), bucketMetadata);
  }

}
