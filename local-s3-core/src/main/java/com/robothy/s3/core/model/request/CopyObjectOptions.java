package com.robothy.s3.core.model.request;

import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class CopyObjectOptions {

  private String sourceBucket;

  private String sourceKey;

  private String sourceVersion;

  public Optional<String> getSourceVersion() {
    return Optional.ofNullable(sourceVersion);
  }

}