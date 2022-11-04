package com.robothy.s3.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(exclude = "inputStream")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VersionedObject {

  private S3Object s3Object;

  private String versionId;

  private long lastModified;

  private String contentType;

  @JsonIgnore
  private InputStream inputStream;

}
