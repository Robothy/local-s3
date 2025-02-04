package com.robothy.s3.core.model.answers;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListMultipartUploadsAns {
  private String bucket;
  private String keyMarker;
  private String uploadIdMarker;
  private String nextKeyMarker;
  private String prefix;
  private String delimiter;
  private String nextUploadIdMarker;
  private int maxUploads;
  private boolean truncated;
  private String encodingType;
  private List<String> commonPrefixes;
  private List<UploadItem> uploads;

  @Data
  @Builder
  public static class UploadItem {
    private String key;
    private String uploadId;
    private long initiated;
  }
}