package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CopyObjectAns {

  private String etag;

  private String sourceVersionId;

  private String versionId;

  private long lastModified;
}
