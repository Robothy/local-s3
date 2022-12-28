package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetObjectTaggingAns {

  private String versionId;

  private String[][] tagging;

}
