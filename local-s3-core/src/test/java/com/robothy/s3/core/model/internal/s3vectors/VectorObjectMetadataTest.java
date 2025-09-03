package com.robothy.s3.core.model.internal.s3vectors;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VectorObjectMetadata.
 */
class VectorObjectMetadataTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void defaultConstructor_createsInstanceWithDefaults() {
    VectorObjectMetadata metadata = new VectorObjectMetadata();

    assertNull(metadata.getVectorId());
    assertEquals(0, metadata.getDimension());
    assertNull(metadata.getMetadata());
    assertNull(metadata.getStorageId());
    assertEquals(0, metadata.getCreationDate());
  }

  @Test
  void parameterizedConstructor_setsAllFields() throws Exception {
    String vectorId = "test-vector";
    int dimension = 128;
    Long storageId = 12345L;
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");
    long beforeCreation = System.currentTimeMillis();

    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata(vectorId, dimension, storageId, metadata);

    long afterCreation = System.currentTimeMillis();

    assertEquals(vectorId, vectorMetadata.getVectorId());
    assertEquals(dimension, vectorMetadata.getDimension());
    assertEquals(storageId, vectorMetadata.getStorageId());
    assertEquals(metadata, vectorMetadata.getMetadata());
    assertTrue(vectorMetadata.getCreationDate() >= beforeCreation);
    assertTrue(vectorMetadata.getCreationDate() <= afterCreation);
  }

  @Test
  void parameterizedConstructor_withNullMetadata_setsNullMetadata() {
    String vectorId = "test-vector";
    int dimension = 128;
    Long storageId = 12345L;

    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata(vectorId, dimension, storageId, null);

    assertEquals(vectorId, vectorMetadata.getVectorId());
    assertEquals(dimension, vectorMetadata.getDimension());
    assertEquals(storageId, vectorMetadata.getStorageId());
    assertNull(vectorMetadata.getMetadata());
  }

  @Test
  void parameterizedConstructor_withNullStorageId_setsNullStorageId() throws Exception {
    String vectorId = "test-vector";
    int dimension = 128;
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata(vectorId, dimension, null, metadata);

    assertEquals(vectorId, vectorMetadata.getVectorId());
    assertEquals(dimension, vectorMetadata.getDimension());
    assertNull(vectorMetadata.getStorageId());
    assertEquals(metadata, vectorMetadata.getMetadata());
  }

  @Test
  void validateDimension_withMatchingDimension_doesNotThrow() {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setDimension(128);

    assertDoesNotThrow(() -> metadata.validateDimension(128));
  }

  @Test
  void validateDimension_withMismatchedDimension_throwsIllegalArgumentException() {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setDimension(128);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.validateDimension(256));

    assertEquals("Vector dimension mismatch. Expected: 256, got: 128", exception.getMessage());
  }

  @Test
  void validateDimension_withZeroDimensions_throwsIllegalArgumentException() {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setDimension(0);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.validateDimension(128));

    assertEquals("Vector dimension mismatch. Expected: 128, got: 0", exception.getMessage());
  }

  @Test
  void validateDimension_withNegativeDimensions_throwsIllegalArgumentException() {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setDimension(-5);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.validateDimension(128));

    assertEquals("Vector dimension mismatch. Expected: 128, got: -5", exception.getMessage());
  }

  @Test
  void settersAndGetters_workCorrectly() throws Exception {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    String vectorId = "updated-vector";
    int dimension = 512;
    Long storageId = 99999L;
    JsonNode jsonMetadata = objectMapper.readTree("{\"tag\": \"updated\"}");
    long creationDate = System.currentTimeMillis();

    metadata.setVectorId(vectorId);
    metadata.setDimension(dimension);
    metadata.setStorageId(storageId);
    metadata.setMetadata(jsonMetadata);
    metadata.setCreationDate(creationDate);

    assertEquals(vectorId, metadata.getVectorId());
    assertEquals(dimension, metadata.getDimension());
    assertEquals(storageId, metadata.getStorageId());
    assertEquals(jsonMetadata, metadata.getMetadata());
    assertEquals(creationDate, metadata.getCreationDate());
  }

  @Test
  void equals_withSameContent_returnsTrue() throws Exception {
    JsonNode metadata1 = objectMapper.readTree("{\"category\": \"test\"}");
    JsonNode metadata2 = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata1);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 1L, metadata2);
    
    // Set same creation date to ensure equality
    vector2.setCreationDate(vector1.getCreationDate());

    assertEquals(vector1, vector2);
  }

  @Test
  void equals_withDifferentVectorId_returnsFalse() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector2", 128, 1L, metadata);

    assertNotEquals(vector1, vector2);
  }

  @Test
  void equals_withDifferentDimension_returnsFalse() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 256, 1L, metadata);

    assertNotEquals(vector1, vector2);
  }

  @Test
  void equals_withDifferentStorageId_returnsFalse() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 2L, metadata);

    assertNotEquals(vector1, vector2);
  }

  @Test
  void equals_withDifferentMetadata_returnsFalse() throws Exception {
    JsonNode metadata1 = objectMapper.readTree("{\"category\": \"test1\"}");
    JsonNode metadata2 = objectMapper.readTree("{\"category\": \"test2\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata1);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 1L, metadata2);

    assertNotEquals(vector1, vector2);
  }

  @Test
  void equals_withNullMetadata_handlesCorrectly() {
    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, null);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 1L, null);
    
    // Set same creation date to ensure equality
    vector2.setCreationDate(vector1.getCreationDate());

    assertEquals(vector1, vector2);
  }

  @Test
  void equals_withOneNullMetadata_returnsFalse() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, null);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 1L, metadata);

    assertNotEquals(vector1, vector2);
  }

  @Test
  void hashCode_withSameContent_returnsSameHashCode() throws Exception {
    JsonNode metadata1 = objectMapper.readTree("{\"category\": \"test\"}");
    JsonNode metadata2 = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata1);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector1", 128, 1L, metadata2);
    
    // Set same creation date to ensure equality
    vector2.setCreationDate(vector1.getCreationDate());

    assertEquals(vector1.hashCode(), vector2.hashCode());
  }

  @Test
  void hashCode_withDifferentContent_returnsDifferentHashCode() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");

    VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 128, 1L, metadata);
    VectorObjectMetadata vector2 = new VectorObjectMetadata("vector2", 128, 1L, metadata);

    assertNotEquals(vector1.hashCode(), vector2.hashCode());
  }

  @Test
  void toString_containsAllFields() throws Exception {
    JsonNode metadata = objectMapper.readTree("{\"category\": \"test\"}");
    VectorObjectMetadata vector = new VectorObjectMetadata("test-vector", 128, 12345L, metadata);

    String toString = vector.toString();

    assertTrue(toString.contains("test-vector"));
    assertTrue(toString.contains("128"));
    assertTrue(toString.contains("12345"));
    assertTrue(toString.contains("VectorObjectMetadata"));
  }

  @Test
  void toString_withNullFields_handlesGracefully() {
    VectorObjectMetadata vector = new VectorObjectMetadata();

    String toString = vector.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("VectorObjectMetadata"));
  }

  @Test
  void edge_case_withComplexJsonMetadata_handlesCorrectly() throws Exception {
    JsonNode complexMetadata = objectMapper.readTree(
        "{\"nested\": {\"array\": [1, 2, 3], \"string\": \"value\"}, \"boolean\": true}");

    VectorObjectMetadata vector = new VectorObjectMetadata("complex-vector", 256, 999L, complexMetadata);

    assertEquals("complex-vector", vector.getVectorId());
    assertEquals(256, vector.getDimension());
    assertEquals(999L, vector.getStorageId());
    assertEquals(complexMetadata, vector.getMetadata());
    assertTrue(complexMetadata.get("nested").get("array").isArray());
    assertTrue(complexMetadata.get("boolean").asBoolean());
  }
}
