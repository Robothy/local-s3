package com.robothy.s3.core.model.answers;

import com.robothy.s3.datatypes.response.VersionItem;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListObjectVersionsAns {

  private String nextKeyMarker;

  private String nextVersionIdMarker;

  private List<VersionItem> versions;

  private List<String> commonPrefixes;

  public Optional<String> getNextKeyMarker() {
    return Optional.ofNullable(nextKeyMarker);
  }

  public Optional<String> getNextVersionIdMarker() {
    return Optional.ofNullable(nextVersionIdMarker);
  }

}
