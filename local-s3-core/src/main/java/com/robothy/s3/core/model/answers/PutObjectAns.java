package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PutObjectAns {

  private String key;

  private String versionId;

  private String etag;

  private long creationDate;

}
