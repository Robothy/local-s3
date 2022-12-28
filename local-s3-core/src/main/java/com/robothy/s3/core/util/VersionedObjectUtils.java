package com.robothy.s3.core.util;

import com.robothy.s3.core.asserionts.VersionedObjectAssertions;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import java.util.Objects;

public class VersionedObjectUtils {

  /**
   * Get versioned object by version ID.
   *
   * @param objectMetadata object metadata that contains the versioned object.
   * @param inputVersionId version ID. May be {@code null}.
   * @return versioned object metadata of the specified version ID.
   */
  public static VersionedObjectMetadata getVersionedObjectMetadata(ObjectMetadata objectMetadata, String inputVersionId) {
    if (Objects.isNull(inputVersionId)) {
      return objectMetadata.getLatest();
    }

    if (ObjectMetadata.NULL_VERSION.equals(inputVersionId)) {
      return VersionedObjectAssertions.assertVirtualVersionExist(objectMetadata);
    }

    return VersionedObjectAssertions.assertVersionedObjectExist(objectMetadata, inputVersionId);
  }

  /**
   * Resolve the returned version if the input version ID is null; or else return the input version ID.
   *
   * @param objectMetadata object metadata.
   * @param inputVersionId input version ID. May be {@code null}.
   * @return resolved return version.
   */
  public static String resolveReturnedVersion(ObjectMetadata objectMetadata, String inputVersionId) {
    if (Objects.isNull(inputVersionId)) {
      if (objectMetadata.getVirtualVersion().map(virtualVersion -> virtualVersion.equals(objectMetadata.getLatestVersion())).orElse(false)) {
        return ObjectMetadata.NULL_VERSION;
      }

      return objectMetadata.getLatestVersion();
    }

    return inputVersionId;
  }

}