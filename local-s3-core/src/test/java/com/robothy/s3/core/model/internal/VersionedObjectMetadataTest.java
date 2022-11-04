package com.robothy.s3.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.util.JsonUtils;
import org.junit.jupiter.api.Test;

class VersionedObjectMetadataTest {

  @Test
  void serialization() {
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    versionedObjectMetadata.setFileId(1L);
    versionedObjectMetadata.setCreationDate(System.currentTimeMillis());
    versionedObjectMetadata.setContentType("application/json");

    String json = JsonUtils.toJson(versionedObjectMetadata);
    VersionedObjectMetadata deserialized = JsonUtils.fromJson(json, VersionedObjectMetadata.class);
    assertEquals(versionedObjectMetadata, deserialized);
  }

}