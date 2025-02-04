package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.answers.ListMultipartUploadsAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.util.S3ObjectUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public interface ListMultipartUploadsService extends LocalS3MetadataApplicable {

  default ListMultipartUploadsAns listMultipartUploads(
      String bucketName,
      String delimiter,
      String encodingType,
      String keyMarker,
      int maxUploads,
      String prefix,
      String uploadIdMarker
  ) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);

    NavigableMap<String, NavigableMap<String, UploadMetadata>> filteredByKeyMarkerAndDelimiter = ListItemUtils
        .filterByKeyMarkerAndDelimiter(bucketMetadata.getUploads(), keyMarker, delimiter);
    NavigableMap<String, NavigableMap<String, UploadMetadata>> filteredByPrefix = ListItemUtils
        .filterByPrefix(filteredByKeyMarkerAndDelimiter, prefix);
    ListMultipartUploadsAns listMultipartUploadsAns =
        listMultipartUploads(filteredByPrefix, delimiter, maxUploads, uploadIdMarker);
    return encodeResultIfNeeded(listMultipartUploadsAns, encodingType);
  }

  static ListMultipartUploadsAns listMultipartUploads(
      NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads,
      String delimiter,
      int maxUploads,
      String uploadIdMarker
  ) {

    List<ListMultipartUploadsAns.UploadItem> listedUploads = new ArrayList<>();
    Set<String> listedCommonPrefixes = new TreeSet<>();
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder()
        .delimiter(delimiter)
        .maxUploads(maxUploads)
        .build();

    String nextKeyMarker = null;
    String nextUploadIdMarker = null;
    String lastKey = null;
    for (Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator = uploads.entrySet().iterator();
         iterator.hasNext(); ) {
      Map.Entry<String, NavigableMap<String, UploadMetadata>> entry = iterator.next();
      String key = entry.getKey();
      if (Objects.nonNull(delimiter) && key.contains(delimiter)) {
        listedCommonPrefixes.add(ListItemUtils.calculateCommonPrefix(key, delimiter));
      } else {
        NavigableMap<String, UploadMetadata> uploadMetadataMap = entry.getValue();
        if (uploadMetadataMap == null || uploadMetadataMap.isEmpty()) {
          continue;
        }

        NavigableMap<String, UploadMetadata> uploadsFilteredByUploadIdMarker = filterByUploadIdMarker(uploadMetadataMap, uploadIdMarker);
        for (Iterator<Map.Entry<String, UploadMetadata>> uploadItr = uploadsFilteredByUploadIdMarker.entrySet().iterator();
             uploadItr.hasNext(); ) {
          Map.Entry<String, UploadMetadata> uploadEntry = uploadItr.next();
          UploadMetadata upload = uploadEntry.getValue();
          listedUploads.add(ListMultipartUploadsAns.UploadItem.builder()
              .key(key)
              .uploadId(uploadEntry.getKey())
              .initiated(upload.getCreateDate())
              .build());
          if (listedUploads.size() + listedCommonPrefixes.size() >= maxUploads) {
            if (uploadItr.hasNext()) {
              nextKeyMarker = lastKey;
              nextUploadIdMarker = uploadEntry.getKey();
            }
            break;
          }
        }
      }

      if (listedUploads.size() + listedCommonPrefixes.size() >= maxUploads) {
        boolean isCalculatedNextMarkers = Objects.nonNull(nextKeyMarker);
        if (!isCalculatedNextMarkers) {
          nextKeyMarker = calculateNextKeyMarker(iterator, key, delimiter);
          nextUploadIdMarker = null;
        }
        break;
      }

      lastKey = key;
    }

    result.setNextKeyMarker(nextKeyMarker);
    result.setUploadIdMarker(nextUploadIdMarker);
    result.setUploads(listedUploads);
    result.setTruncated(Objects.nonNull(nextKeyMarker));
    result.setCommonPrefixes(new ArrayList<>(listedCommonPrefixes));
    return result;
  }


  static String calculateNextKeyMarker(Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator,
                                       String currentKey,
                                       String delimiter) {
    if (Objects.isNull(delimiter) || !currentKey.contains(delimiter)) {
      return iterator.hasNext() ? currentKey : null;
    }

    String commonPrefix = ListItemUtils.calculateCommonPrefix(currentKey, delimiter);
    while (iterator.hasNext()) {
      if (!iterator.next().getKey().startsWith(commonPrefix)) {
        return commonPrefix;
      }
    }
    return null;
  }

  static ListMultipartUploadsAns encodeResultIfNeeded(ListMultipartUploadsAns result, String encodingType) {
    if (Objects.isNull(encodingType)) {
      return result;
    }

    if (!"url".equalsIgnoreCase(encodingType)) {
      throw new LocalS3InvalidArgumentException("encoding-type", encodingType, "Invalid Encoding Method specified in Request");
    }

    result.setEncodingType(encodingType);
    result.getUploads().forEach(uploadItem -> {
      uploadItem.setKey(S3ObjectUtils.urlEncodeEscapeSlash(uploadItem.getKey()));
    });

    List<String> encodedPrefixes = new ArrayList<>(result.getCommonPrefixes().size());
    result.getCommonPrefixes().forEach(commonPrefix ->
        encodedPrefixes.add(S3ObjectUtils.urlEncodeEscapeSlash(commonPrefix)));
    result.setCommonPrefixes(encodedPrefixes);
    result.setDelimiter(S3ObjectUtils.urlEncodeEscapeSlash(result.getDelimiter()));
    result.setPrefix(S3ObjectUtils.urlEncodeEscapeSlash(result.getPrefix()));
    result.setKeyMarker(S3ObjectUtils.urlEncodeEscapeSlash(result.getKeyMarker()));
    result.setNextKeyMarker(S3ObjectUtils.urlEncodeEscapeSlash(result.getNextKeyMarker()));
    result.setUploadIdMarker(S3ObjectUtils.urlEncodeEscapeSlash(result.getUploadIdMarker()));
    result.setNextUploadIdMarker(S3ObjectUtils.urlEncodeEscapeSlash(result.getNextUploadIdMarker()));
    return result;
  }


  static NavigableMap<String, UploadMetadata> filterByUploadIdMarker(NavigableMap<String, UploadMetadata> uploadMetadataMap, String uploadIdMarker) {
    if (Objects.isNull(uploadIdMarker)) {
      return uploadMetadataMap;
    }
    return uploadMetadataMap.tailMap(uploadIdMarker, false);
  }

}
