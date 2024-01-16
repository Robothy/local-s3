package com.robothy.s3.core.model.answers;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetObjectAns {

  private String bucketName;

  private String key;

  private String versionId;

  private boolean deleteMarker;

  private String contentType;

  private long size;

  private long lastModified;

  private String etag;

  private InputStream content;

  private Map<String, String> userMetadata;

  private int taggingCount;
}
