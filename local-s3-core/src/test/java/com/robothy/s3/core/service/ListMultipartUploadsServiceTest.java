package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.answers.ListMultipartUploadsAns;
import com.robothy.s3.core.model.internal.UploadMetadata;
import java.util.Collections;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map;
import com.robothy.s3.core.model.answers.ListMultipartUploadsAns.UploadItem;
import com.robothy.s3.core.util.S3ObjectUtils;
import java.util.Arrays;

class ListMultipartUploadsServiceTest {

  @Test
  void testEncodeResultIfNeeded() {
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder()
        .prefix("test/prefix")
        .delimiter("/")
        .keyMarker("keyMarker")
        .nextKeyMarker("nextKeyMarker")
        .uploadIdMarker("uploadIdMarker")
        .nextUploadIdMarker("nextUploadIdMarker")
        .uploads(Collections.singletonList(UploadItem.builder().key("key1").build()))
        .commonPrefixes(Collections.singletonList("prefix1"))
        .build();

    ListMultipartUploadsAns encodedResult = ListMultipartUploadsService.encodeResultIfNeeded(result, "url");

    assertEquals("test/prefix", encodedResult.getPrefix());
    assertEquals("/", encodedResult.getDelimiter());
    assertEquals("keyMarker", encodedResult.getKeyMarker());
    assertEquals("nextKeyMarker", encodedResult.getNextKeyMarker());
    assertEquals("uploadIdMarker", encodedResult.getUploadIdMarker());
    assertEquals("nextUploadIdMarker", encodedResult.getNextUploadIdMarker());
  }

  @Test
  void testEncodeResultIfNeededWithInvalidEncodingType() {
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder().build();

    assertThrows(LocalS3InvalidArgumentException.class, () -> {
      ListMultipartUploadsService.encodeResultIfNeeded(result, "invalid");
    });
  }

  @Test
  void testListMultipartUploads() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = new TreeMap<>();
    NavigableMap<String, UploadMetadata> uploadMap = new TreeMap<>();
    uploadMap.put("uploadId1", new UploadMetadata());
    uploads.put("key1", uploadMap);

    ListMultipartUploadsAns result = ListMultipartUploadsService.listMultipartUploads(uploads, "/", 1, null);

    assertEquals(1, result.getUploads().size());
    assertEquals("key1", result.getUploads().get(0).getKey());
    assertEquals("uploadId1", result.getUploads().get(0).getUploadId());
  }

  @Test
  void testListMultipartUploadsWithDelimiter() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = new TreeMap<>();
    NavigableMap<String, UploadMetadata> uploadMap = new TreeMap<>();
    uploadMap.put("uploadId1", new UploadMetadata());
    uploads.put("key1/test", uploadMap);

    ListMultipartUploadsAns result = ListMultipartUploadsService.listMultipartUploads(uploads, "/", 1, null);

    assertEquals(1, result.getCommonPrefixes().size());
    assertEquals("key1/", result.getCommonPrefixes().get(0));
  }

  @Test
  void testListMultipartUploadsWithUploadIdMarker() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = new TreeMap<>();
    NavigableMap<String, UploadMetadata> uploadMap = new TreeMap<>();
    uploadMap.put("uploadId1", new UploadMetadata());
    uploadMap.put("uploadId2", new UploadMetadata());
    uploads.put("key1", uploadMap);

    ListMultipartUploadsAns result = ListMultipartUploadsService.listMultipartUploads(uploads, null, 1, "uploadId1");

    assertEquals(1, result.getUploads().size());
    assertEquals("key1", result.getUploads().get(0).getKey());
    assertEquals("uploadId2", result.getUploads().get(0).getUploadId());
  }


  @Test
  void testListMultipartUploadsWithMaxUploads() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> uploads = new TreeMap<>();
    NavigableMap<String, UploadMetadata> uploadMetadataMap1 = new TreeMap<>();
    uploadMetadataMap1.put("uploadId1", new UploadMetadata(123456789L, "contentType", null, null, null));
    uploads.put("key1", uploadMetadataMap1);

    NavigableMap<String, UploadMetadata> uploadMetadataMap2 = new TreeMap<>();
    uploadMetadataMap2.put("uploadId2", new UploadMetadata(987654321L, "contentType", null, null, null));
    uploads.put("key2", uploadMetadataMap2);

    ListMultipartUploadsAns result = ListMultipartUploadsService.listMultipartUploads(uploads, null, 1, null);

    assertEquals(1, result.getUploads().size());
    assertTrue(result.isTruncated());
    assertEquals("key1", result.getNextKeyMarker());
    assertNull(result.getNextUploadIdMarker());
  }

  @Test
  void testCalculateNextKeyMarker_NoDelimiter() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> map = new TreeMap<>();
    map.put("key1", new TreeMap<>());
    map.put("key2", new TreeMap<>());
    Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator = map.entrySet().iterator();
    String nextKeyMarker = ListMultipartUploadsService.calculateNextKeyMarker(iterator, "key1", null);
    assertEquals("key1", nextKeyMarker);
  }

  @Test
  void testCalculateNextKeyMarker_WithDelimiter_NoCommonPrefix() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> map = new TreeMap<>();
    map.put("key1/part1", new TreeMap<>());
    map.put("key2/part1", new TreeMap<>());
    Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator = map.entrySet().iterator();
    String nextKeyMarker = ListMultipartUploadsService.calculateNextKeyMarker(iterator, "key1/part1", "/");
    assertEquals("key1/", nextKeyMarker);
  }

  @Test
  void testCalculateNextKeyMarker_WithDelimiter_CommonPrefix() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> map = new TreeMap<>();
    map.put("key1/part1", new TreeMap<>());
    map.put("key1/part2", new TreeMap<>());
    map.put("key2/part1", new TreeMap<>());
    Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator = map.entrySet().iterator();
    String nextKeyMarker = ListMultipartUploadsService.calculateNextKeyMarker(iterator, "key1/part1", "/");
    assertEquals("key1/", nextKeyMarker);
  }

  @Test
  void testCalculateNextKeyMarker_NoMoreKeys() {
    NavigableMap<String, NavigableMap<String, UploadMetadata>> map = new TreeMap<>();
    map.put("key1/part1", new TreeMap<>());
    Iterator<Map.Entry<String, NavigableMap<String, UploadMetadata>>> iterator = map.entrySet().iterator();
    String nextKeyMarker = ListMultipartUploadsService.calculateNextKeyMarker(iterator, "key1/part1", "/");
    assertNull(nextKeyMarker);
  }

  @Test
  void testEncodeResultIfNeeded_NullEncodingType() {
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder()
        .uploads(Arrays.asList(UploadItem.builder().key("key1").build()))
        .commonPrefixes(Arrays.asList("prefix1"))
        .delimiter("delimiter")
        .prefix("prefix")
        .keyMarker("keyMarker")
        .nextKeyMarker("nextKeyMarker")
        .uploadIdMarker("uploadIdMarker")
        .nextUploadIdMarker("nextUploadIdMarker")
        .build();

    ListMultipartUploadsAns encodedResult = ListMultipartUploadsService.encodeResultIfNeeded(result, null);

    assertEquals(result, encodedResult);
  }

  @Test
  void testEncodeResultIfNeeded_InvalidEncodingType() {
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder().build();

    assertThrows(LocalS3InvalidArgumentException.class, () -> {
      ListMultipartUploadsService.encodeResultIfNeeded(result, "invalid");
    });
  }

  @Test
  void testEncodeResultIfNeeded_UrlEncodingType() {
    ListMultipartUploadsAns result = ListMultipartUploadsAns.builder()
        .uploads(Arrays.asList(UploadItem.builder().key("key1").build()))
        .commonPrefixes(Arrays.asList("prefix1"))
        .delimiter("delimiter")
        .prefix("prefix")
        .keyMarker("keyMarker")
        .nextKeyMarker("nextKeyMarker")
        .uploadIdMarker("uploadIdMarker")
        .nextUploadIdMarker("nextUploadIdMarker")
        .build();

    ListMultipartUploadsAns encodedResult = ListMultipartUploadsService.encodeResultIfNeeded(result, "url");

    assertEquals("url", encodedResult.getEncodingType());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("key1"), encodedResult.getUploads().get(0).getKey());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("prefix1"), encodedResult.getCommonPrefixes().get(0));
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("delimiter"), encodedResult.getDelimiter());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("prefix"), encodedResult.getPrefix());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("keyMarker"), encodedResult.getKeyMarker());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("nextKeyMarker"), encodedResult.getNextKeyMarker());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("uploadIdMarker"), encodedResult.getUploadIdMarker());
    assertEquals(S3ObjectUtils.urlEncodeEscapeSlash("nextUploadIdMarker"), encodedResult.getNextUploadIdMarker());
  }

}