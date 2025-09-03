package com.robothy.s3.datatypes.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.datatypes.s3vectors.request.CreateIndexRequest;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VectorIndexTest {

  @Test
  void constructor_withDefaultValues_setsCorrectDefaults() {
    VectorIndex index = new VectorIndex();
    
    assertNull(index.getCreationTime());
    assertNull(index.getDataType());
    assertEquals(0, index.getDimension());
    assertNull(index.getDistanceMetric());
    assertNull(index.getIndexArn());
    assertNull(index.getIndexName());
    assertNull(index.getMetadataConfiguration());
    assertNull(index.getVectorBucketName());
    assertNull(index.getStatus());
    assertNull(index.getVectorCount());
  }

  @Test
  void builder_withAllFields_setsCorrectValues() {
    Long creationTime = 1693776000L;
    CreateIndexRequest.MetadataConfiguration metadataConfig = CreateIndexRequest.MetadataConfiguration.builder().build();
    
    VectorIndex index = VectorIndex.builder()
        .creationTime(creationTime)
        .dataType(VectorDataType.FLOAT32)
        .dimension(512)
        .distanceMetric(DistanceMetric.COSINE)
        .indexArn("arn:aws:s3vectors:::vector-bucket/test-bucket/index/test-index")
        .indexName("test-index")
        .metadataConfiguration(metadataConfig)
        .vectorBucketName("test-bucket")
        .status("ACTIVE")
        .vectorCount(1000L)
        .build();

    assertEquals(creationTime, index.getCreationTime());
    assertEquals(VectorDataType.FLOAT32, index.getDataType());
    assertEquals(512, index.getDimension());
    assertEquals(DistanceMetric.COSINE, index.getDistanceMetric());
    assertEquals("arn:aws:s3vectors:::vector-bucket/test-bucket/index/test-index", index.getIndexArn());
    assertEquals("test-index", index.getIndexName());
    assertEquals(metadataConfig, index.getMetadataConfiguration());
    assertEquals("test-bucket", index.getVectorBucketName());
    assertEquals("ACTIVE", index.getStatus());
    assertEquals(1000L, index.getVectorCount());
  }

  @Test
  void setDimension_withValidValue_setsDimension() {
    VectorIndex index = new VectorIndex();
    
    index.setDimension(256);
    
    assertEquals(256, index.getDimension());
  }

  @Test
  void setDimension_withMinimumValue_setsDimension() {
    VectorIndex index = new VectorIndex();
    
    index.setDimension(1);
    
    assertEquals(1, index.getDimension());
  }

  @Test
  void setDimension_withMaximumValue_setsDimension() {
    VectorIndex index = new VectorIndex();
    
    index.setDimension(4096);
    
    assertEquals(4096, index.getDimension());
  }

  @Test
  void setDimension_withZero_throwsException() {
    VectorIndex index = new VectorIndex();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
        () -> index.setDimension(0));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: 0", exception.getMessage());
  }

  @Test
  void setDimension_withNegativeValue_throwsException() {
    VectorIndex index = new VectorIndex();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
        () -> index.setDimension(-1));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: -1", exception.getMessage());
  }

  @Test
  void setDimension_withValueTooLarge_throwsException() {
    VectorIndex index = new VectorIndex();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
        () -> index.setDimension(4097));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: 4097", exception.getMessage());
  }

  @Test
  void validateDimension_withValidValue_doesNotThrow() {
    assertDoesNotThrow(() -> VectorIndex.validateDimension(512));
  }

  @Test
  void validateDimension_withMinimumValue_doesNotThrow() {
    assertDoesNotThrow(() -> VectorIndex.validateDimension(1));
  }

  @Test
  void validateDimension_withMaximumValue_doesNotThrow() {
    assertDoesNotThrow(() -> VectorIndex.validateDimension(4096));
  }

  @Test
  void validateDimension_withInvalidValue_throwsException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
        () -> VectorIndex.validateDimension(0));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: 0", exception.getMessage());
  }

  @Test
  void generateArn_withValidParameters_returnsCorrectArn() {
    String arn = VectorIndex.generateArn("my-bucket", "my-index");
    
    assertEquals("arn:aws:s3vectors:::vector-bucket/my-bucket/index/my-index", arn);
  }

  @Test
  void generateArn_withSpecialCharacters_returnsCorrectArn() {
    String arn = VectorIndex.generateArn("bucket-with-hyphens", "index_with_underscores");
    
    assertEquals("arn:aws:s3vectors:::vector-bucket/bucket-with-hyphens/index/index_with_underscores", arn);
  }

  @Test
  void setCreationTimeFromInstant_withValidInstant_setsCreationTime() {
    VectorIndex index = new VectorIndex();
    Instant instant = Instant.ofEpochSecond(1693776000L);
    
    index.setCreationTimeFromInstant(instant);
    
    assertEquals(1693776000L, index.getCreationTime());
  }

  @Test
  void setCreationTimeFromInstant_withNull_setsNull() {
    VectorIndex index = new VectorIndex();
    
    index.setCreationTimeFromInstant(null);
    
    assertNull(index.getCreationTime());
  }

  @Test
  void getCreationTimeAsInstant_withValidTime_returnsInstant() {
    VectorIndex index = new VectorIndex();
    index.setCreationTime(1693776000L);
    
    Instant result = index.getCreationTimeAsInstant();
    
    assertEquals(Instant.ofEpochSecond(1693776000L), result);
  }

  @Test
  void getCreationTimeAsInstant_withNullTime_returnsNull() {
    VectorIndex index = new VectorIndex();
    index.setCreationTime(null);
    
    Instant result = index.getCreationTimeAsInstant();
    
    assertNull(result);
  }

  @Test
  void equals_withSameValues_returnsTrue() {
    VectorIndex index1 = VectorIndex.builder()
        .indexName("test-index")
        .dimension(512)
        .dataType(VectorDataType.FLOAT32)
        .build();
    
    VectorIndex index2 = VectorIndex.builder()
        .indexName("test-index")
        .dimension(512)
        .dataType(VectorDataType.FLOAT32)
        .build();
    
    assertEquals(index1, index2);
  }

  @Test
  void equals_withDifferentValues_returnsFalse() {
    VectorIndex index1 = VectorIndex.builder()
        .indexName("test-index-1")
        .dimension(512)
        .build();
    
    VectorIndex index2 = VectorIndex.builder()
        .indexName("test-index-2")
        .dimension(512)
        .build();
    
    assertNotEquals(index1, index2);
  }

  @Test
  void hashCode_withSameValues_returnsSameHashCode() {
    VectorIndex index1 = VectorIndex.builder()
        .indexName("test-index")
        .dimension(512)
        .build();
    
    VectorIndex index2 = VectorIndex.builder()
        .indexName("test-index")
        .dimension(512)
        .build();
    
    assertEquals(index1.hashCode(), index2.hashCode());
  }

  @Test
  void toString_withValues_containsFieldValues() {
    VectorIndex index = VectorIndex.builder()
        .indexName("test-index")
        .dimension(512)
        .vectorBucketName("test-bucket")
        .build();
    
    String toString = index.toString();
    
    assertTrue(toString.contains("test-index"));
    assertTrue(toString.contains("512"));
    assertTrue(toString.contains("test-bucket"));
  }
}
