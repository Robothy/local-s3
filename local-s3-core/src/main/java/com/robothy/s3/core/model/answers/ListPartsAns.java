package com.robothy.s3.core.model.answers;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ListPartsAns {

  private String bucket;

  private String key;

  private String uploadId;

  private Integer partNumberMarker;

  private Integer nextPartNumberMarker;

  private Integer maxParts;

  private boolean isTruncated;

  private List<Part> parts;

  @Data
  @Builder
  public static class Part {

    private Integer partNumber;

    private long lastModified;

    private String eTag;

    private long size;

  }

}
