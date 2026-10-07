package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.datatypes.s3vectors.response.ListTagsForResourceResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for TagResourceService, UntagResourceService and ListTagsForResourceService.
 */
class VectorsTaggingServiceTest {

  @Test
  void tagResource_withValidTags_addsTags() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    service.tagResource("test-bucket", singletonTag("env", "prod"));

    Map<String, String> tags = service.listTagsForResource("test-bucket").getTags();
    assertEquals(1, tags.size());
    assertEquals("prod", tags.get("env"));
  }

  @Test
  void tagResource_withMultipleTags_addsAllTags() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    Map<String, String> newTags = new HashMap<>();
    newTags.put("env", "prod");
    newTags.put("team", "core");
    service.tagResource("test-bucket", newTags);

    Map<String, String> tags = service.listTagsForResource("test-bucket").getTags();
    assertEquals(2, tags.size());
    assertEquals("prod", tags.get("env"));
    assertEquals("core", tags.get("team"));
  }

  @Test
  void tagResource_withExistingKey_overwritesValueAndKeepsOtherTags() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    service.tagResource("test-bucket", singletonTag("env", "dev"));
    service.tagResource("test-bucket", singletonTag("env", "prod"));

    Map<String, String> tags = service.listTagsForResource("test-bucket").getTags();
    assertEquals(1, tags.size());
    assertEquals("prod", tags.get("env"));
  }

  @Test
  void untagResource_removesOnlySpecifiedKeys() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    Map<String, String> newTags = new HashMap<>();
    newTags.put("env", "prod");
    newTags.put("team", "core");
    service.tagResource("test-bucket", newTags);

    service.untagResource("test-bucket", Collections.singletonList("env"));

    Map<String, String> tags = service.listTagsForResource("test-bucket").getTags();
    assertEquals(1, tags.size());
    assertFalse(tags.containsKey("env"));
    assertEquals("core", tags.get("team"));
  }

  @Test
  void untagResource_withNonExistentKey_doesNotThrow() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    service.tagResource("test-bucket", singletonTag("env", "prod"));

    service.untagResource("test-bucket", Arrays.asList("missing", "env"));

    Map<String, String> tags = service.listTagsForResource("test-bucket").getTags();
    assertTrue(tags.isEmpty());
  }

  @Test
  void listTagsForResource_onFreshBucket_returnsEmptyTags() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    ListTagsForResourceResponse response = service.listTagsForResource("test-bucket");

    assertNotNull(response);
    assertNotNull(response.getTags());
    assertTrue(response.getTags().isEmpty());
  }

  @Test
  void untagAllTags_resultsInEmptyTagSet() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    service.tagResource("test-bucket", singletonTag("env", "prod"));
    service.untagResource("test-bucket", Collections.singletonList("env"));

    assertTrue(service.listTagsForResource("test-bucket").getTags().isEmpty());
  }

  @Test
  void operations_onMissingBucket_throwNotFound() {
    TaggingServiceImpl service = new TaggingServiceImpl();

    LocalS3VectorException tagException = assertThrows(LocalS3VectorException.class,
        () -> service.tagResource("missing-bucket", singletonTag("env", "prod")));
    assertEquals(LocalS3VectorErrorType.NOT_FOUND, tagException.getErrorType());

    LocalS3VectorException untagException = assertThrows(LocalS3VectorException.class,
        () -> service.untagResource("missing-bucket", Collections.singletonList("env")));
    assertEquals(LocalS3VectorErrorType.NOT_FOUND, untagException.getErrorType());

    LocalS3VectorException listException = assertThrows(LocalS3VectorException.class,
        () -> service.listTagsForResource("missing-bucket"));
    assertEquals(LocalS3VectorErrorType.NOT_FOUND, listException.getErrorType());
  }

  @Test
  void tagResource_withNullOrEmptyTags_throwsInvalidRequest() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    LocalS3VectorException nullException = assertThrows(LocalS3VectorException.class,
        () -> service.tagResource("test-bucket", null));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, nullException.getErrorType());

    LocalS3VectorException emptyException = assertThrows(LocalS3VectorException.class,
        () -> service.tagResource("test-bucket", Collections.emptyMap()));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, emptyException.getErrorType());
  }

  @Test
  void tagResource_withBlankTagValue_throwsInvalidRequest() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> service.tagResource("test-bucket", singletonTag("env", " ")));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
  }

  @Test
  void untagResource_withNullOrEmptyTagKeys_throwsInvalidRequest() {
    TaggingServiceImpl service = new TaggingServiceImpl();
    service.bucketMap.put("test-bucket", newBucket("test-bucket"));

    LocalS3VectorException nullException = assertThrows(LocalS3VectorException.class,
        () -> service.untagResource("test-bucket", null));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, nullException.getErrorType());

    LocalS3VectorException emptyException = assertThrows(LocalS3VectorException.class,
        () -> service.untagResource("test-bucket", Collections.emptyList()));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, emptyException.getErrorType());
  }

  @Test
  void tagResource_withBlankBucketName_throwsInvalidRequest() {
    TaggingServiceImpl service = new TaggingServiceImpl();

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> service.tagResource(" ", singletonTag("env", "prod")));
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
  }

  @Test
  void putTag_withNullKey_throwsIllegalArgument() {
    VectorBucketMetadata bucketMetadata = newBucket("test-bucket");

    assertThrows(IllegalArgumentException.class, () -> bucketMetadata.putTag(null, "value"));
    assertThrows(IllegalArgumentException.class, () -> bucketMetadata.putTag("  ", "value"));
  }

  @Test
  void removeTag_withNonExistentKey_returnsNull() {
    VectorBucketMetadata bucketMetadata = newBucket("test-bucket");

    assertNull(bucketMetadata.removeTag("missing"));
  }

  @Test
  void getTags_afterNullDeserialization_returnsNonNullMap() {
    VectorBucketMetadata bucketMetadata = newBucket("test-bucket");
    bucketMetadata.setTags(null);

    assertNotNull(bucketMetadata.getTags());
    assertTrue(bucketMetadata.getTags().isEmpty());
  }

  private static VectorBucketMetadata newBucket(String name) {
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName(name);
    return bucketMetadata;
  }

  private static Map<String, String> singletonTag(String key, String value) {
    return Collections.singletonMap(key, value);
  }

  /**
   * Test implementation of the tagging services for testing purposes.
   */
  private static class TaggingServiceImpl implements TagResourceService, UntagResourceService,
      ListTagsForResourceService {
    private final LocalS3VectorsMetadata mockMetadata = mock(LocalS3VectorsMetadata.class);
    private final Map<String, VectorBucketMetadata> bucketMap = new HashMap<>();

    public TaggingServiceImpl() {
      when(mockMetadata.getVectorBucketMetadataMap()).thenReturn(bucketMap);
    }

    @Override
    public LocalS3VectorsMetadata metadata() {
      return mockMetadata;
    }
  }
}
