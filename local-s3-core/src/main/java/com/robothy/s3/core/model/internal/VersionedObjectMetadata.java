package com.robothy.s3.core.model.internal;

import lombok.Data;

@Data
public class VersionedObjectMetadata {

  private String etag;

  private String contentType;

  private long creationDate;

  @Deprecated
  private long modificationDate;

  private long size;

  /**
   * Is this versioned object deleted.
   */
  private boolean isDeleted;

  /**
   * File ID in {@linkplain com.robothy.s3.core.storage.Storage}.
   */
  private Long fileId;

}
