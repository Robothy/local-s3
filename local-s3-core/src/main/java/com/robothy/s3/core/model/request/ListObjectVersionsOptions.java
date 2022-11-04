package com.robothy.s3.core.model.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class ListObjectVersionsOptions {

  private String bucketName;

  private String key;

  private String delimiter;

  private String encodingType;

  private String keyMarker;

  @Builder.Default
  private int maxKeys = 1000;

  private String prefix;

  private String versionIdMarker;

}
