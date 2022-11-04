package com.robothy.s3.core.model.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPartMetadata {

  private String etag;

  private long lastModified;

  private long size;

  private long fileId;

}
