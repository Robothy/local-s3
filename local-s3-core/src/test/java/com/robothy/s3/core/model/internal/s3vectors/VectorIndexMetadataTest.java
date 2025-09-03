package com.robothy.s3.core.model.internal.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.VectorDataType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class VectorIndexMetadataTest {

  @Test
  void constructor_withDefaultValues_setsCorrectDefaults() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    assertNull(metadata.getIndexName());
    assertEquals(0, metadata.getDimension());
    assertNull(metadata.getDataType());
    assertNull(metadata.getDistanceMetric());
    assertNull(metadata.getMetadataSchema());
    assertNull(metadata.getNonFilterableMetadataKeys());
    assertEquals(0, metadata.getCreationDate());
    assertEquals("CREATING", metadata.getStatus());
    assertNotNull(metadata.getVectorObjects());
    assertTrue(metadata.getVectorObjects().isEmpty());
    assertInstanceOf(ConcurrentHashMap.class, metadata.getVectorObjects());
  }

  @Test
  void setDimension_withValidValue_setsDimension() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDimension(512);
    
    assertEquals(512, metadata.getDimension());
  }

  @Test
  void setDimension_withMinimumValue_setsDimension() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDimension(1);
    
    assertEquals(1, metadata.getDimension());
  }

  @Test
  void setDimension_withMaximumValue_setsDimension() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDimension(4096);
    
    assertEquals(4096, metadata.getDimension());
  }

  @Test
  void setDimension_withZero_throwsException() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.setDimension(0));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: 0", exception.getMessage());
  }

  @Test
  void setDimension_withNegativeValue_throwsException() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.setDimension(-1));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: -1", exception.getMessage());
  }

  @Test
  void setDimension_withValueTooLarge_throwsException() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.setDimension(4097));
    
    assertEquals("Vector dimension must be between 1 and 4096, got: 4097", exception.getMessage());
  }

  @Test
  void isActive_withActiveStatus_returnsTrue() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setActive();
    
    assertTrue(metadata.isActive());
  }

  @Test
  void isActive_withCreatingStatus_returnsFalse() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    assertFalse(metadata.isActive());
  }

  @Test
  void isActive_withFailedStatus_returnsFalse() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setFailed();
    
    assertFalse(metadata.isActive());
  }

  @Test
  void setActive_changesStatusToActive() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setActive();
    
    assertEquals("ACTIVE", metadata.getStatus());
  }

  @Test
  void setFailed_changesStatusToFailed() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setFailed();
    
    assertEquals("FAILED", metadata.getStatus());
  }

  @Test
  void setDeleting_changesStatusToDeleting() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDeleting();
    
    assertEquals("DELETING", metadata.getStatus());
  }

  @Test
  void addVectorObject_withValidVector_addsToMap() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(3);
    
    metadata.addVectorObject(vectorMetadata);
    
    assertEquals(1, metadata.getVectorObjectCount());
    assertEquals(vectorMetadata, metadata.getVectorObject("vector1"));
  }

  @Test
  void addVectorObject_withIncompatibleDimension_throwsException() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(5); // Different dimension
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> metadata.addVectorObject(vectorMetadata));
    
    assertTrue(exception.getMessage().contains("dimension"));
  }

  @Test
  void getVectorObject_withExistingVector_returnsVector() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(3);
    metadata.addVectorObject(vectorMetadata);
    
    VectorObjectMetadata result = metadata.getVectorObject("vector1");
    
    assertEquals(vectorMetadata, result);
  }

  @Test
  void getVectorObject_withNonExistentVector_returnsNull() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    VectorObjectMetadata result = metadata.getVectorObject("non-existent");
    
    assertNull(result);
  }

  @Test
  void removeVectorObject_withExistingVector_removesAndReturnsVector() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(3);
    metadata.addVectorObject(vectorMetadata);
    
    VectorObjectMetadata result = metadata.removeVectorObject("vector1");
    
    assertEquals(vectorMetadata, result);
    assertEquals(0, metadata.getVectorObjectCount());
    assertFalse(metadata.containsVectorObject("vector1"));
  }

  @Test
  void removeVectorObject_withNonExistentVector_returnsNull() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    VectorObjectMetadata result = metadata.removeVectorObject("non-existent");
    
    assertNull(result);
  }

  @Test
  void containsVectorObject_withExistingVector_returnsTrue() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(3);
    metadata.addVectorObject(vectorMetadata);
    
    assertTrue(metadata.containsVectorObject("vector1"));
  }

  @Test
  void containsVectorObject_withNonExistentVector_returnsFalse() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    assertFalse(metadata.containsVectorObject("non-existent"));
  }

  @Test
  void getVectorObjectCount_withEmptyIndex_returnsZero() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    assertEquals(0, metadata.getVectorObjectCount());
  }

  @Test
  void getVectorObjectCount_withMultipleVectors_returnsCorrectCount() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    
    for (int i = 0; i < 5; i++) {
      VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
      vectorMetadata.setVectorId("vector" + i);
      vectorMetadata.setDimension(3);
      metadata.addVectorObject(vectorMetadata);
    }
    
    assertEquals(5, metadata.getVectorObjectCount());
  }

  @Test
  void clearVectorObjects_removesAllVectors() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId("vector1");
    vectorMetadata.setDimension(3);
    metadata.addVectorObject(vectorMetadata);
    
    metadata.clearVectorObjects();
    
    assertEquals(0, metadata.getVectorObjectCount());
    assertFalse(metadata.containsVectorObject("vector1"));
  }

  @Test
  void setIndexName_withValidName_setsName() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setIndexName("test-index");
    
    assertEquals("test-index", metadata.getIndexName());
  }

  @Test
  void setDataType_withValidType_setsDataType() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDataType(VectorDataType.FLOAT32);
    
    assertEquals(VectorDataType.FLOAT32, metadata.getDataType());
  }

  @Test
  void setDistanceMetric_withValidMetric_setsDistanceMetric() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    
    metadata.setDistanceMetric(DistanceMetric.COSINE);
    
    assertEquals(DistanceMetric.COSINE, metadata.getDistanceMetric());
  }

  @Test
  void setCreationDate_withValidDate_setsCreationDate() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    long creationDate = System.currentTimeMillis();
    
    metadata.setCreationDate(creationDate);
    
    assertEquals(creationDate, metadata.getCreationDate());
  }

  @Test
  void threadSafety_concurrentVectorOperations_handlesCorrectly() {
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setDimension(3);
    
    // Verify concurrent map is thread-safe
    assertDoesNotThrow(() -> {
      VectorObjectMetadata vector1 = new VectorObjectMetadata();
      vector1.setVectorId("vector1");
      vector1.setDimension(3);
      
      VectorObjectMetadata vector2 = new VectorObjectMetadata();
      vector2.setVectorId("vector2");
      vector2.setDimension(3);
      
      metadata.addVectorObject(vector1);
      metadata.addVectorObject(vector2);
      metadata.removeVectorObject("vector1");
    });
  }
}
