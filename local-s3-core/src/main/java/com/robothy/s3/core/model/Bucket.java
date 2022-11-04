package com.robothy.s3.core.model;

import com.robothy.s3.core.model.internal.BucketMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Bucket {

  private String name;

  /**
   * null - default, user not set versioning.
   * true - enabled
   * false - suspended
   */
  private Boolean versioningEnabled;

  private long creationDate;

  /**
   * Create a {@linkplain Bucket} instance from {@linkplain BucketMetadata}.
   * The {@linkplain BucketMetadata} contains all properties of {@linkplain Bucket}.
   *
   * @param bucketMetadata bucket metadata instance.
   * @return a new {@linkplain Bucket} instance.
   */
  public static Bucket fromBucketMetadata(BucketMetadata bucketMetadata) {
    return Bucket.builder()
        .name(bucketMetadata.getBucketName())
        .versioningEnabled(bucketMetadata.getVersioningEnabled())
        .creationDate(bucketMetadata.getCreationDate())
        .build();
  }

}
