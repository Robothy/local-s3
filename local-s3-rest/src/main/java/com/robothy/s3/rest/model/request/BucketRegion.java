package com.robothy.s3.rest.model.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
@ToString
public class BucketRegion {

  private final String region;

  private final String bucketName;

  public Optional<String> getBucketName() {
    return Optional.ofNullable(bucketName);
  }

}
