package com.robothy.s3.core.model.request;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(exclude = "content")
public class PutObjectOptions {

  private String contentType;

  private long size;

  private InputStream content;

  private String contentMd5;

  private String[][] tagging;

  private Map<String, String> userMetadata;

  /**
   * Get tagging in the put object request.
   *
   * @return tagging.
   */
  public Optional<String[][]> getTagging() {
    return Optional.ofNullable(tagging);
  }

}
