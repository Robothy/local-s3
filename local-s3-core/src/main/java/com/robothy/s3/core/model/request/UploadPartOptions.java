package com.robothy.s3.core.model.request;

import java.io.InputStream;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadPartOptions {

  private long contentLength;

  private InputStream data;

}
