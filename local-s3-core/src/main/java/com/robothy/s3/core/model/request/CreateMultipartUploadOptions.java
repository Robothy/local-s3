package com.robothy.s3.core.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMultipartUploadOptions {

  private String contentType;

}
