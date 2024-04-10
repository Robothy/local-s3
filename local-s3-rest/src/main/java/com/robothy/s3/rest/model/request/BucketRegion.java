package com.robothy.s3.rest.model.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class BucketRegion {

  private final String region;

  private final String bucketName;

  public Optional<String> getBucketName() {
    return Optional.ofNullable(bucketName);
  }

}
