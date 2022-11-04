package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.enums.StorageClass;
import com.robothy.s3.datatypes.response.Object;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Algorithm implementation of list objects.
 */
public interface ListObjectsService extends LocalS3MetadataApplicable {

  ListObjectsAns EMPTY_RESULT = new ListObjectsAns();

  /**
   * List objects with options.
   *
   * @param bucket the bucket to list objects.
   * @param delimiter the delimiter for condensing common prefixes in the returned listing results.
   * @param marker the marker indicating where the returned results should begin.
   * @param maxKeys the maximum objects to return.
   * @param prefix the prefix restricting what keys will be listed.
   * @return a listing of objects from the specified bucket.
   */
  default ListObjectsAns listObjects(String bucket, Character delimiter, String marker, int maxKeys, String prefix) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);

    NavigableMap<String, ObjectMetadata> filteredWithMarker = bucketMetadata.getObjectMap();
    if (Objects.nonNull(marker)) {
      ObjectAssertions.assertObjectExists(bucketMetadata, marker);
      ConcurrentSkipListMap<String, ObjectMetadata> allObjects = bucketMetadata.getObjectMap();
      filteredWithMarker = allObjects.tailMap(marker, false);
    }

    if (filteredWithMarker.isEmpty()) {
      return EMPTY_RESULT;
    }

    List<Object> objects = new LinkedList<>();
    Set<String> commonPrefixes = new TreeSet<>();
    int prefixLen = Objects.isNull(prefix) ? 0 : prefix.length();
    boolean hasMore = false;
    String nextKeyMarker = null;
    for (String key : filteredWithMarker.keySet()) {
      if (filteredWithMarker.get(key).getLatest().isDeleted()) {
        continue;
      }

      if (Objects.nonNull(prefix) && !key.startsWith(prefix)) {
        continue;
      }

      int keyCount = commonPrefixes.size() + objects.size();
      if (keyCount == maxKeys) {
        hasMore = true;
        break;
      }

      if (keyCount < maxKeys) {
        int prefixOccursAt;
        if (Objects.nonNull(delimiter) && (-1 != (prefixOccursAt = key.indexOf(delimiter, prefixLen)))) {
          commonPrefixes.add(key.substring(0, prefixOccursAt + 1));
        } else {
          objects.add( fetchLatestObject(key, filteredWithMarker.get(key)));
        }
        nextKeyMarker = key;
      }
    }

    return ListObjectsAns.builder()
        .nextMarker(hasMore ? nextKeyMarker : null)
        .objects(objects)
        .commonPrefixes(new ArrayList<>(commonPrefixes))
        .build();
  }


  private Object fetchLatestObject(String key, ObjectMetadata objectMetadata) {
    VersionedObjectMetadata latest = objectMetadata.getLatest();
    Object object = new Object();
    object.setKey(key);
    object.setSize(latest.getSize());
    object.setLastModified(new Date(latest.getModificationDate()));
    object.setEtag(latest.getEtag());
    object.setOwner(Owner.DEFAULT_OWNER);
    object.setStorageClass(StorageClass.STANDARD);
    return object;
  }

}
