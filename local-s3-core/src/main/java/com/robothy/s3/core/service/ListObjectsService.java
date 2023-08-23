package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.ObjectAssertions;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Algorithm implementation of list objects.
 */
public interface ListObjectsService extends LocalS3MetadataApplicable {

  ListObjectsAns EMPTY_RESULT = new ListObjectsAns();

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
  default ListObjectsAns listObjects(String bucket, Character delimiter, String encodingType,
                                     String marker, int maxKeys, String prefix) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);
    NavigableMap<String, ObjectMetadata> objectsAfterMarker = fetchObjectsAfterMarker(bucketMetadata, marker);
    ListObjectsAns listObjectsAns = listObjects(objectsAfterMarker, delimiter, maxKeys, prefix);
    listObjectsAns.setMarker(marker);
    encodeIfNeeded(listObjectsAns, encodingType);
    return listObjectsAns;
  }

  static NavigableMap<String, ObjectMetadata> fetchObjectsAfterMarker(BucketMetadata bucketMetadata, String marker) {
    if (Objects.isNull(marker)) {
      return bucketMetadata.getObjectMap();
    }

    ObjectAssertions.assertObjectExists(bucketMetadata, marker);
    return bucketMetadata.getObjectMap().tailMap(marker, false);
  }

  static ListObjectsAns listObjects(NavigableMap<String, ObjectMetadata> objectsAfterMarker, Character delimiter, int maxKeys, String prefix) {
    if (objectsAfterMarker.isEmpty()) {
      return EMPTY_RESULT;
    }

    List<S3Object> objects = new LinkedList<>();
    Set<String> commonPrefixes = new TreeSet<>();
    int prefixLen = Objects.isNull(prefix) ? 0 : prefix.length();
    boolean hasMore = false;
    String nextKeyMarker = null;
    for (String key : objectsAfterMarker.keySet()) {
      if (objectsAfterMarker.get(key).getLatest().isDeleted()) {
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
          objects.add(fetchLatestObject(key, objectsAfterMarker.get(key)));
        }
        nextKeyMarker = key;
      }
    }

    return ListObjectsAns.builder()
        .delimiter(Objects.nonNull(delimiter) ? delimiter.toString() : null)
        .maxKeys(maxKeys)
        .prefix(prefix)
        .nextMarker((hasMore && Objects.nonNull(delimiter)) ? nextKeyMarker : null)
        .isTruncated(hasMore)
        .objects(objects)
        .commonPrefixes(new ArrayList<>(commonPrefixes))
        .build();
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
