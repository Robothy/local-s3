package com.robothy.s3.core.model.answers;

import com.robothy.s3.datatypes.response.S3Object;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of list objects.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListObjectsAns {

  /**
   * The delimiter returned in the response. The delimiter will be encoded if the encoding type is not empty.
   */
  private String delimiter;

  private String encodingType;

  /**
   * Maker returned in the response. The marker will be encoded if the encoding type is not empty.
   */
  private String marker;

  private int maxKeys;

  /**
   * Represents the first key of subsequent objects if the result is truncated.
   * Its value is {@code null} if the result is not truncated.
   */
  private String nextMarker;

  private boolean isTruncated;

  /**
   * The prefix returned in the response. The prefix will be encoded if the encoding type is not empty.
   */
  private String prefix;

  @Builder.Default
  private List<S3Object> objects = Collections.emptyList();

  @Builder.Default
  private List<String> commonPrefixes = Collections.emptyList();

  public Optional<String> getNextMarker() {
    return Optional.ofNullable(nextMarker);
  }

}
