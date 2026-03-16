package com.robothy.s3.core.model.request;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetObjectOptions {

  @Deprecated
  private String bucketName;

  @Deprecated
  private String key;

  private String versionId;

  private Range range;

  public Optional<String> getVersionId() {
    return Optional.ofNullable(versionId);
  }

  public Optional<Range> getRange() {
    return Optional.ofNullable(range);
  }

}
