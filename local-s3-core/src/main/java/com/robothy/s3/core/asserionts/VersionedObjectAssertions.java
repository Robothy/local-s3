package com.robothy.s3.core.asserionts;

import com.robothy.s3.core.exception.VersionedObjectNotExistException;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;

public class VersionedObjectAssertions {

  /**
   * Asset that the specified object and version is exist in the bucket.
   * @param objectMetadata object metadata.
   * @param version object version.
   * @return the {@linkplain VersionedObjectMetadata} of the specified version.
   */
  public static VersionedObjectMetadata assertVersionedObjectExist(ObjectMetadata objectMetadata, String version) {
    return objectMetadata.getVersionedObjectMetadata(version)
        .orElseThrow(() -> new VersionedObjectNotExistException(version));
  }

  /**
   * Assert that the specified {@linkplain ObjectMetadata} has virtual version.
   *
   * @param objectMetadata the object metadata.
   * @return the {@linkplain VersionedObjectMetadata} instance.
   */
  public static VersionedObjectMetadata assertVirtualVersionExist(ObjectMetadata objectMetadata) {
    return objectMetadata.getVirtualVersion().map(virtualVersion -> objectMetadata.getVersionedObjectMap().get(virtualVersion))
        .orElseThrow(() -> new VersionedObjectNotExistException(ObjectMetadata.NULL_VERSION));
  }

}
