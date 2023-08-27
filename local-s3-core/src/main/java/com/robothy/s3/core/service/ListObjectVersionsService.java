package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.asserionts.VersionedObjectAssertions;
import com.robothy.s3.core.model.answers.ListObjectVersionsAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.enums.StorageClass;
import com.robothy.s3.datatypes.response.DeleteMarkerEntry;
import com.robothy.s3.datatypes.response.ObjectVersion;
import com.robothy.s3.datatypes.response.VersionItem;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

public interface ListObjectVersionsService extends LocalS3MetadataApplicable {


  default ListObjectVersionsAns listObjectVersions(String bucket, String delimiter, String keyMarker, int maxKeys, String prefix, String versionIdMarker) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);

    if (Objects.nonNull(versionIdMarker) && Objects.isNull(keyMarker)) {
      throw new IllegalArgumentException("A version-id marker cannot be specified without a key marker.");
    }

    List<VersionItem> versionItems = new LinkedList<>();
    List<String> commonPrefixes = new LinkedList<>();

    int prefixLen = Objects.isNull(prefix) ? 0 : prefix.length();
    Set<String> candidateKeys;
    String nextVersionIdMarker;
    String nextKeyMarker;
    int delimiterIndex;
    if (Objects.nonNull(keyMarker)) {
      ObjectMetadata objectMetadata = ObjectAssertions.assertObjectExists(bucketMetadata, keyMarker);

      // If the keyMarker doesn't have a common prefix.
      if (Objects.isNull(delimiter) || -1 == (delimiterIndex = keyMarker.indexOf(delimiter, prefixLen))) {
        NavigableMap<String, VersionedObjectMetadata> versions;
        if (Objects.nonNull(versionIdMarker)) {
          String realVersionId;
          if (ObjectMetadata.NULL_VERSION.equals(versionIdMarker)) {
            VersionedObjectAssertions.assertVirtualVersionExist(objectMetadata);
            realVersionId = objectMetadata.getVirtualVersion().get();
          } else {
            VersionedObjectAssertions.assertVersionedObjectExist(objectMetadata, versionIdMarker);
            realVersionId = versionIdMarker;
          }
          versions = objectMetadata.getVersionedObjectMap().tailMap(realVersionId, false);
        } else {
          versions = objectMetadata.getVersionedObjectMap();
        }

        // If the keyMarker match the prefix condition, then fetch related versions.
        if (Objects.isNull(prefix) || keyMarker.startsWith(prefix)) {
          nextVersionIdMarker = fetchVersions(versionItems, commonPrefixes, keyMarker, versions, false, maxKeys,
              objectMetadata.getVirtualVersion().orElse(null));
        } else {
          nextVersionIdMarker = versions.lastKey();
        }
      } else { // The keyMarker has a common prefix
        commonPrefixes.add(keyMarker.substring(0, delimiterIndex + 1));
        nextVersionIdMarker = objectMetadata.getVersionedObjectMap().lastKey();
      }

      nextKeyMarker = keyMarker;
      candidateKeys = bucketMetadata.getObjectMap().tailMap(keyMarker, false).keySet();
    } else {
      nextVersionIdMarker = nextKeyMarker = null;
      candidateKeys = bucketMetadata.getObjectMap().keySet();
    }

    int keyCount = versionItems.size() + commonPrefixes.size();
    if (keyCount == maxKeys) {
      return ListObjectVersionsAns.builder()
          .nextKeyMarker(nextKeyMarker)
          .nextVersionIdMarker(nextVersionIdMarker)
          .versions(versionItems)
          .commonPrefixes(commonPrefixes)
          .build();
    }

    /*-- Process remaining keys. --*/

    for (String key : candidateKeys) {
      if (Objects.nonNull(prefix) && !key.startsWith(prefix)) {
        continue;
      }

      ObjectMetadata objectMetadata = bucketMetadata.getObjectMetadata(key).get();
      if (Objects.nonNull(delimiter) && -1 != (delimiterIndex = key.indexOf(delimiter, prefixLen))) {
        commonPrefixes.add(key.substring(0, delimiterIndex + 1));
        nextVersionIdMarker = null;
      } else {
        nextVersionIdMarker = fetchVersions(versionItems, commonPrefixes, key, objectMetadata.getVersionedObjectMap(), true, maxKeys, objectMetadata.getVirtualVersion().orElse(null));
      }
      nextKeyMarker = key;

      if ((keyCount = commonPrefixes.size() + versionItems.size()) == maxKeys) {
        break;
      }
    }

    return ListObjectVersionsAns.builder()
        .nextKeyMarker(keyCount == maxKeys ? nextKeyMarker : null)
        .nextVersionIdMarker((keyCount == maxKeys) ? nextVersionIdMarker : null)
        .versions(versionItems)
        .commonPrefixes(commonPrefixes)
        .build();
  }

  /**
   * Fetch {@code versions} to {@code versionItems}.
   *
   * @param versionItems where version items store.
   * @param commonPrefixes fetched common prefixes.
   * @param versions where version items fetch from.
   * @param maxKeys max keys.
   * @return last visited version ID.
   */
  static String fetchVersions(List<VersionItem> versionItems, List<String> commonPrefixes, String key,
                               Map<String, VersionedObjectMetadata> versions, boolean firstItemIsLatest,
                               int maxKeys, String virtualVersion) {

    String lastVisitedVersion = null;
    int keyCount = versionItems.size() + commonPrefixes.size();
    boolean isLatest = firstItemIsLatest;
    for (Map.Entry<String, VersionedObjectMetadata> entry : versions.entrySet()) {
      if (keyCount == maxKeys) {
        break;
      }

      VersionedObjectMetadata versionedObjectMetadata = entry.getValue();

      if (versionedObjectMetadata.isDeleted()) {
        versionItems.add(DeleteMarkerEntry.builder()
            .latest(isLatest)
            .versionId(entry.getKey().equals(virtualVersion) ? ObjectMetadata.NULL_VERSION : entry.getKey())
            .key(key)
            .lastModified(Instant.ofEpochMilli(versionedObjectMetadata.getCreationDate()))
            .build());
      } else {
        versionItems.add(ObjectVersion.builder()
            .latest(isLatest)
            .versionId(entry.getKey().equals(virtualVersion) ? ObjectMetadata.NULL_VERSION : entry.getKey())
            .key(key)
            .lastModified(Instant.ofEpochMilli(versionedObjectMetadata.getCreationDate()))
            .size(versionedObjectMetadata.getSize())
            .etag(versionedObjectMetadata.getEtag())
            .storageClass(StorageClass.STANDARD)
            .owner(Owner.DEFAULT_OWNER)
            .build());
      }

      lastVisitedVersion = entry.getKey();
      isLatest = false;
      keyCount++;
    }

    return lastVisitedVersion;
  }


}
