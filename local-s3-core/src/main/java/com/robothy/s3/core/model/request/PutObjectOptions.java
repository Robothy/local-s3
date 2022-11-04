package com.robothy.s3.core.model.request;

import java.io.InputStream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(exclude = "content")
public class PutObjectOptions {

  private String contentType;

  private long size;

  private InputStream content;

}
