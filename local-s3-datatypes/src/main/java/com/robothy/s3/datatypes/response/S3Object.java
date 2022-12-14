package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import com.robothy.s3.datatypes.enums.CheckSumAlgorithm;
import com.robothy.s3.datatypes.enums.StorageClass;
import java.time.Instant;
import java.util.Comparator;
import lombok.Data;

/**
 * Represents S3 <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Object.html">Object</a>.
 */
@Data
public class S3Object {

  /**
   * Order by key ascending.
   */

  public static final Comparator<S3Object> ORDER_BY_KEY_ASC = Comparator.comparing(S3Object::getKey);

  /**
   * Order by last modified descending.
   */
  public static final Comparator<S3Object> ORDER_BY_LAST_MODIFIED_DESC = Comparator
      .comparing(S3Object::getLastModified).reversed();

  @JsonProperty("ChecksumAlgorithm")
  private CheckSumAlgorithm checkSumAlgorithm;

  @JsonProperty("ETag")
  private String etag;

  @JsonProperty("Key")
  private String key;

  @JsonProperty("LastModified")
  @JsonSerialize(converter = AmazonInstantConverter.class)
  private Instant lastModified;

  @JsonProperty("Owner")
  private Owner owner;

  @JsonProperty("Size")
  private long size;

  @JsonProperty("StorageClass")
  private StorageClass storageClass;

}
