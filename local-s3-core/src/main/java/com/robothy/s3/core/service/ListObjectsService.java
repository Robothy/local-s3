package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.internal.VersionedObjectMetadata;
import com.robothy.s3.core.util.S3ObjectUtils;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.enums.StorageClass;
import com.robothy.s3.datatypes.response.S3Object;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Algorithm implementation of list objects.
 */
public interface ListObjectsService extends LocalS3MetadataApplicable {

  ConcurrentSkipListMap<String, ObjectMetadata> EMPTY_OBJECT_MAP = new ConcurrentSkipListMap<>();

  /**
   * List objects with options.
   *
   * @param bucket       the bucket to list objects.
   * @param delimiter    the delimiter for condensing common prefixes in the returned listing results.
   * @param encodingType the encoding method for keys in the returned listing results.
   * @param marker       the marker indicating where the returned results should begin.
   * @param maxKeys      the maximum objects to return.
   * @param prefix       the prefix restricting what keys will be listed.
   * @return a listing of objects from the specified bucket.
   */
  default ListObjectsAns listObjects(String bucket, String delimiter, String encodingType,
                                     String marker, int maxKeys, String prefix) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);

    String originalDelimiter = delimiter;
    if (Objects.nonNull(prefix) && Objects.nonNull(delimiter) && prefix.contains(delimiter)) {
      delimiter = null;
    }
    NavigableMap<String, ObjectMetadata> objectsAfterMarker = filterByMarkerDelimiter(bucketMetadata, marker, delimiter);
    NavigableMap<String, ObjectMetadata> filteredByPrefix = filterByPrefix(objectsAfterMarker, prefix);

    ListObjectsAns listObjectsAns = listObjectsAndCommonPrefixes(filteredByPrefix, delimiter, maxKeys);
    listObjectsAns.setDelimiter(originalDelimiter);
    listObjectsAns.setMarker(Objects.isNull(marker) ? "" : marker);
    listObjectsAns.setPrefix(Objects.isNull(prefix) ? "" : prefix);
    encodeIfNeeded(listObjectsAns, encodingType);
    return listObjectsAns;
  }

  static NavigableMap<String, ObjectMetadata> filterByMarkerDelimiter(BucketMetadata bucketMetadata, String marker, String delimiter) {
    if (Objects.isNull(marker)) {
      return bucketMetadata.getObjectMap();
    }

    NavigableMap<String, ObjectMetadata> filteredByMarker = bucketMetadata.getObjectMap().tailMap(marker, false);
    if (Objects.isNull(delimiter) || filteredByMarker.isEmpty()) {
      return filteredByMarker;
    }

    String firstKey = filteredByMarker.firstKey();
    String firstKeyCommonPrefix;
    if (!firstKey.contains(delimiter) || (firstKeyCommonPrefix = calculateCommonPrefix(firstKey, delimiter)).compareTo(marker) > 0) {
      return filteredByMarker;
    }

    String fromKey = filteredByMarker.ceilingKey(firstKeyCommonPrefix + Character.MAX_VALUE);
    if (Objects.isNull(fromKey)) {
      return EMPTY_OBJECT_MAP;
    }
    return filteredByMarker.tailMap(fromKey, true);
  }



  static NavigableMap<String, ObjectMetadata> filterByPrefix(NavigableMap<String, ObjectMetadata> objectsAfterMarker, String prefix) {
    if (Objects.isNull(prefix) || objectsAfterMarker.isEmpty()) {
      return objectsAfterMarker;
    }

    String fromKey = objectsAfterMarker.floorKey(prefix);
    String toKey = objectsAfterMarker.floorKey(prefix + Character.MAX_VALUE);
    if (Objects.isNull(toKey)) {
      return EMPTY_OBJECT_MAP;
    }

    if (Objects.isNull(fromKey)) {
      return objectsAfterMarker.headMap(toKey, true);
    }

    boolean fromKeyInclusive = fromKey.startsWith(prefix);
    return objectsAfterMarker.subMap(fromKey, fromKeyInclusive, toKey, true);
  }

  static ListObjectsAns listObjectsAndCommonPrefixes(NavigableMap<String, ObjectMetadata> filteredObjects, String delimiter, int maxKeys) {
    if (filteredObjects.isEmpty() || 0 == maxKeys) {
      return ListObjectsAns.builder()
        .delimiter(delimiter)
        .maxKeys(maxKeys)
        .build();
    }

    List<S3Object> objects = new LinkedList<>();
    Set<String> commonPrefixes = new TreeSet<>();


    String nextMarker = null;

    for (Iterator<String> keyIterator = filteredObjects.keySet().iterator(); keyIterator.hasNext(); ) {
      String key = keyIterator.next();
      if (filteredObjects.get(key).getLatest().isDeleted()) {
        continue;
      }

      if (Objects.nonNull(delimiter) && key.contains(delimiter)) {
        commonPrefixes.add(calculateCommonPrefix(key, delimiter));
      } else {
        objects.add(fetchLatestObject(key, filteredObjects.get(key)));
      }

      int keyCount = commonPrefixes.size() + objects.size();

      if (keyCount == maxKeys) {
        nextMarker = calculateNextMarker(filteredObjects, key, keyIterator, delimiter);
        break;
      }

    }

    return ListObjectsAns.builder()
      .delimiter(delimiter)
      .maxKeys(maxKeys)
      .nextMarker(nextMarker)
      .isTruncated(Objects.nonNull(nextMarker))
      .objects(objects)
      .commonPrefixes(new ArrayList<>(commonPrefixes))
      .build();
  }

  static String calculateNextMarker(NavigableMap<String, ObjectMetadata> filteredObjects,
                                    String currentKey, Iterator<String> keyIterator, String delimiter) {
    if (Objects.isNull(delimiter) || !currentKey.contains(delimiter)) {
      return keyIterator.hasNext() ? currentKey : null;
    }

    String commonPrefix = calculateCommonPrefix(currentKey, delimiter);

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      if (!key.startsWith(commonPrefix) && !filteredObjects.get(key).getLatest().isDeleted()) {
        return commonPrefix;
      }
    }

    return null;
  }

  static String calculateCommonPrefix(String key, String delimiter) {
    return key.substring(0, key.indexOf(delimiter) + delimiter.length());
  }

  static S3Object fetchLatestObject(String key, ObjectMetadata objectMetadata) {
    VersionedObjectMetadata latest = objectMetadata.getLatest();
    S3Object object = new S3Object();
    object.setKey(key);
    object.setSize(latest.getSize());
    object.setLastModified(Instant.ofEpochMilli(latest.getCreationDate()));
    object.setEtag(latest.getEtag());
    object.setOwner(Owner.DEFAULT_OWNER);
    object.setStorageClass(StorageClass.STANDARD);
    return object;
  }

  static void encodeIfNeeded(ListObjectsAns listObjectsAns, String encodingType) {
    if (Objects.isNull(encodingType)) {
      return;
    }

    if (!"url".equalsIgnoreCase(encodingType)) {
      throw new LocalS3InvalidArgumentException("encoding-type", encodingType, "Invalid Encoding Method specified in Request");
    }

    listObjectsAns.setEncodingType(encodingType);
    listObjectsAns.getObjects()
      .forEach(object -> object.setKey(S3ObjectUtils.urlEncodeEscapeSlash(object.getKey())));
    List<String> encodedPrefixes = new ArrayList<>(listObjectsAns.getCommonPrefixes().size());
    listObjectsAns.getCommonPrefixes().forEach(commonPrefix ->
      encodedPrefixes.add(S3ObjectUtils.urlEncodeEscapeSlash(commonPrefix)));
    listObjectsAns.setCommonPrefixes(encodedPrefixes);
    listObjectsAns.setDelimiter(S3ObjectUtils.urlEncodeEscapeSlash(listObjectsAns.getDelimiter()));
    listObjectsAns.setPrefix(S3ObjectUtils.urlEncodeEscapeSlash(listObjectsAns.getPrefix()));
    listObjectsAns.setMarker(S3ObjectUtils.urlEncodeEscapeSlash(listObjectsAns.getMarker()));
    listObjectsAns.setNextMarker(S3ObjectUtils.urlEncodeEscapeSlash(listObjectsAns.getNextMarker().orElse(null)));
  }

}
