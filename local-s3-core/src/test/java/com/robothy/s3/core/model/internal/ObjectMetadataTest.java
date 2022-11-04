package com.robothy.s3.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.core.util.JsonUtils;
import org.junit.jupiter.api.Test;

class ObjectMetadataTest {

  @Test
  void getLatest() {
    String key = "a.txt";
    String versionId = IdUtils.defaultGenerator().nextStrId();
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    ObjectMetadata objectMetadata = new ObjectMetadata(key, versionId, versionedObjectMetadata);
    assertEquals(key, objectMetadata.getKey());
    assertSame(versionedObjectMetadata, objectMetadata.getLatest());
    assertEquals(versionId, objectMetadata.getLatestVersion());

    String latestVersionId = IdUtils.defaultGenerator().nextStrId();
    VersionedObjectMetadata latestVersionedObjectMetadata = new VersionedObjectMetadata();
    objectMetadata.putVersionedObjectMetadata(latestVersionId, latestVersionedObjectMetadata);
    assertSame(latestVersionedObjectMetadata, objectMetadata.getLatest());
    assertEquals(latestVersionId, objectMetadata.getLatestVersion());
  }

  @Test
  void serialize() {
    String key = "a.txt";
    String versionId = IdUtils.defaultGenerator().nextStrId();
    VersionedObjectMetadata versionedObjectMetadata = new VersionedObjectMetadata();
    ObjectMetadata objectMetadata = new ObjectMetadata(key, versionId, versionedObjectMetadata);
    String json = JsonUtils.toJson(objectMetadata);
    ObjectMetadata serialized = JsonUtils.fromJson(json, ObjectMetadata.class);
    assertEquals(objectMetadata, serialized);
  }

}