package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketReadLock;
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
  @BucketReadLock
  default ListObjectsAns listObjects(String bucket, String delimiter, String encodingType,
                                     String marker, int maxKeys, String prefix) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);

    NavigableMap<String, ObjectMetadata> objectsAfterMarker = ListItemUtils.filterByKeyMarker(bucketMetadata.getObjectMap(), marker);
    NavigableMap<String, ObjectMetadata> filteredByPrefix = ListItemUtils.filterByPrefix(objectsAfterMarker, prefix);

    String effectivePrefix = Objects.toString(prefix, "");
    ListObjectsAns listObjectsAns = listObjectsAndCommonPrefixes(filteredByPrefix, effectivePrefix, delimiter, maxKeys);
    listObjectsAns.setDelimiter(delimiter);
    listObjectsAns.setMarker(Objects.isNull(marker) ? "" : marker);
    listObjectsAns.setPrefix(effectivePrefix);
    encodeIfNeeded(listObjectsAns, encodingType);
    return listObjectsAns;
  }

  static ListObjectsAns listObjectsAndCommonPrefixes(NavigableMap<String, ObjectMetadata> filteredObjects, String effectivePrefix, String delimiter, int maxKeys) {
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

      String suffix = key.substring(effectivePrefix.length());
      if (Objects.nonNull(delimiter) && suffix.contains(delimiter)) {
        commonPrefixes.add(effectivePrefix + suffix.substring(0, suffix.indexOf(delimiter) + 1));
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

    String commonPrefix = ListItemUtils.calculateCommonPrefix(currentKey, delimiter);

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      if (!key.startsWith(commonPrefix) && !filteredObjects.get(key).getLatest().isDeleted()) {
        return commonPrefix;
      }
    }

    return null;
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
