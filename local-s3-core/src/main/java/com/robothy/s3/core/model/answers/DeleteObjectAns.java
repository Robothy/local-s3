package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DeleteObjectAns {

  private boolean isDeleteMarker;

  private String versionId;

}
