package com.robothy.s3.core.model.answers;

import com.robothy.s3.datatypes.response.S3Object;
import java.util.LinkedList;
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
   * Represents the first key of subsequent objects if the result is truncated.
   * Its value is {@code null} if the result is not truncated.
   */
  private String nextMarker;

  private List<S3Object> objects = new LinkedList<>();

  private List<String> commonPrefixes = new LinkedList<>();

  public Optional<String> getNextMarker() {
    return Optional.ofNullable(nextMarker);
  }

}
