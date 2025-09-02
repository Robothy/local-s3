package com.robothy.s3.core.model.internal.s3vectors;

import lombok.Getter;

@Getter
public class VectorObjectIdentifier {

  private final String bucketName;

  private final String indexName;

  private final String key;

  public VectorObjectIdentifier(String bucketName, String indexName, String key) {
    this.bucketName = bucketName;
    this.indexName = indexName;
    this.key = key;
  }

}
