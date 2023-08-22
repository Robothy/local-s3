package com.robothy.s3.core.model.answers;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadPartAns {

  private String etag;

}
