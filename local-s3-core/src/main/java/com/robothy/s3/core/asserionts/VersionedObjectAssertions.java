package com.robothy.s3.core.asserionts;

import com.robothy.s3.core.exception.VersionedObjectNotExistException;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;

public class VersionedObjectAssertions {

  public static VersionedObjectMetadata assertVersionedObjectExist(ObjectMetadata objectMetadata, String version) {
    return objectMetadata.getVersionedObjectMetadata(version)
        .orElseThrow(() -> new VersionedObjectNotExistException(objectMetadata.getKey(), version));
  }

  public static VersionedObjectMetadata assertVirtualVersionExist(ObjectMetadata objectMetadata) {
    return objectMetadata.getVirtualVersion().map(virtualVersion -> objectMetadata.getVersionedObjectMap().get(virtualVersion))
        .orElseThrow(() -> new VersionedObjectNotExistException(objectMetadata.getKey(), ObjectMetadata.NULL_VERSION));
  }

}
