package com.robothy.s3.core.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteMultipartUploadPartOption {

  private String etag;

  private int partNumber;

}
