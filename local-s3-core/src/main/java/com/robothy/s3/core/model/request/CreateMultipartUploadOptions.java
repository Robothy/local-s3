package com.robothy.s3.core.model.request;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMultipartUploadOptions {

  private String contentType;

  private String[][] tagging;

  public Optional<String[][]> getTagging() {
    return Optional.ofNullable(tagging);
  }
}
