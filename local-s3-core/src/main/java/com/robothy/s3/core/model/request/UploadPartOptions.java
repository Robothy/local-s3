package com.robothy.s3.core.model.request;

import java.io.InputStream;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadPartOptions {

  private long contentLength;

  private InputStream data;

  private String etag;

  public Optional<String> getETag() {
    return Optional.ofNullable(etag);
  }

}
