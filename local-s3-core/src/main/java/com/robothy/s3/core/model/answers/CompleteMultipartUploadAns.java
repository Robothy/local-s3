package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompleteMultipartUploadAns {

  private String location;

  private String versionId;

  private String etag;

}
