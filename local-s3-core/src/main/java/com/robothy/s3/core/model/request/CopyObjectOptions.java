package com.robothy.s3.core.model.request;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class CopyObjectOptions {

  /**
   * Metadata directive for copy operation.
   */
  public enum MetadataDirective {
    /**
     * Copy metadata from source object (default).
     */
    COPY,

    /**
     * Replace with metadata provided in the request.
     */
    REPLACE
  }

  private String sourceBucket;

  private String sourceKey;

  private String sourceVersion;

  private MetadataDirective metadataDirective;

  private Map<String, String> userMetadata;

  public Optional<String> getSourceVersion() {
    return Optional.ofNullable(sourceVersion);
  }

  public MetadataDirective getMetadataDirective() {
    return metadataDirective != null ? metadataDirective : MetadataDirective.COPY;
  }

  public Map<String, String> getUserMetadata() {
    return userMetadata != null ? userMetadata : Collections.emptyMap();
  }
}