package com.robothy.s3.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3Object {

  private Bucket bucket;

  private String key;

  private String latestVersionId;

}
