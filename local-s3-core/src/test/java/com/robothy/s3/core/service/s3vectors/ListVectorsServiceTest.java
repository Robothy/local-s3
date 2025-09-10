package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import com.robothy.s3.datatypes.s3vectors.ListOutputVector;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorsResponse;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ListVectorsServiceTest {

  @Test
  void listVectors_withValidRequest_returnsVectorList() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    vectorObjects.put("vector2", createVectorMetadata("vector2", 2L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertNotNull(response);
    assertNotNull(response.getVectors());
    assertEquals(2, response.getVectors().size());
    assertEquals("vector1", response.getVectors().get(0).getKey());
    assertEquals("vector2", response.getVectors().get(1).getKey());
  }

  @Test
  void listVectors_withNonExistentBucket_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    
    assertThrows(LocalS3VectorException.class, 
        () -> service.listVectors("nonexistent", "index", null, null, false, false, null, null));
  }

  @Test
  void listVectors_withNonExistentIndex_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "nonexistent", null, null, false, false, null, null));
  }

  @Test
  void listVectors_withMaxResults_limitsResults() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    for (int i = 1; i <= 5; i++) {
      vectorObjects.put("vector" + i, createVectorMetadata("vector" + i, (long) i));
    }
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", 3, null, false, false, null, null);
    
    assertEquals(3, response.getVectors().size());
  }

  @Test
  void listVectors_withMaxResultsExceedingLimit_capsAt1000() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", 2000, null, false, false, null, null);
    
    // Should still return all available vectors (1) despite high maxResults
    assertEquals(1, response.getVectors().size());
  }

  @Test
  void listVectors_withNullMaxResults_usesDefault500() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertNotNull(response);
    assertEquals(1, response.getVectors().size());
  }

  @Test
  void listVectors_withNextToken_startsFromCorrectPosition() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    for (int i = 1; i <= 5; i++) {
      vectorObjects.put("vector" + i, createVectorMetadata("vector" + i, (long) i));
    }
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    String nextToken = Base64.getEncoder().encodeToString("2".getBytes());
    ListVectorsResponse response = service.listVectors("bucket", "index", 2, nextToken, false, false, null, null);
    
    assertEquals(2, response.getVectors().size());
    assertEquals("vector3", response.getVectors().get(0).getKey());
    assertEquals("vector4", response.getVectors().get(1).getKey());
  }

  @Test
  void listVectors_withInvalidNextToken_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, "invalid_token", false, false, null, null));
  }

  @Test
  void listVectors_withEmptyNextToken_startsFromBeginning() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, "", false, false, null, null);
    
    assertEquals(1, response.getVectors().size());
    assertEquals("vector1", response.getVectors().get(0).getKey());
  }

  @Test
  void listVectors_withReturnDataTrue_includesVectorData() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.getVectorData(1L)).thenReturn(new float[]{1.0f, 2.0f});
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, true, false, null, null);
    
    assertEquals(1, response.getVectors().size());
    ListOutputVector vector = response.getVectors().get(0);
    assertNotNull(vector.getData());
    assertArrayEquals(new float[]{1.0f, 2.0f}, vector.getData().getValues());
  }

  @Test
  void listVectors_withReturnDataFalse_excludesVectorData() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertEquals(1, response.getVectors().size());
    assertNull(response.getVectors().get(0).getData());
  }

  @Test
  void listVectors_withReturnMetadataTrue_includesMetadata() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", 1L);
    JsonNode metadata = new ObjectMapper().valueToTree(Map.of("key", "value"));
    vectorMetadata.setMetadata(metadata);
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", vectorMetadata);
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, true, null, null);
    
    assertEquals(1, response.getVectors().size());
    assertEquals(metadata, response.getVectors().get(0).getMetadata());
  }

  @Test
  void listVectors_withReturnMetadataFalse_excludesMetadata() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertEquals(1, response.getVectors().size());
    assertNull(response.getVectors().get(0).getMetadata());
  }

  @Test
  void listVectors_withValidSegmentation_returnsSegmentedResults() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    for (int i = 1; i <= 6; i++) {
      vectorObjects.put("vector" + i, createVectorMetadata("vector" + i, (long) i));
    }
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    // segmentCount=3, segmentIndex=1 should return vectors at positions 1, 4 (0-indexed)
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, 3, 1);
    
    assertEquals(2, response.getVectors().size());
    assertEquals("vector2", response.getVectors().get(0).getKey());
    assertEquals("vector5", response.getVectors().get(1).getKey());
  }

  @Test
  void listVectors_withOnlySegmentCount_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, 3, null));
  }

  @Test
  void listVectors_withOnlySegmentIndex_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, null, 1));
  }

  @Test
  void listVectors_withInvalidSegmentCount_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, 0, 0));
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, 17, 0));
  }

  @Test
  void listVectors_withInvalidSegmentIndex_throwsException() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, 3, -1));
    
    assertThrows(LocalS3VectorException.class,
        () -> service.listVectors("bucket", "index", null, null, false, false, 3, 3));
  }

  @Test
  void listVectors_generatesCorrectNextToken() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    for (int i = 1; i <= 5; i++) {
      vectorObjects.put("vector" + i, createVectorMetadata("vector" + i, (long) i));
    }
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", 2, null, false, false, null, null);
    
    assertNotNull(response.getNextToken());
    String expectedToken = Base64.getEncoder().encodeToString("2".getBytes());
    assertEquals(expectedToken, response.getNextToken());
  }

  @Test
  void listVectors_withNoMoreResults_returnsNullNextToken() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", 10, null, false, false, null, null);
    
    assertNull(response.getNextToken());
  }

  @Test
  void listVectors_withEmptyIndex_returnsEmptyList() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    indexMetadata.setVectorObjects(new ConcurrentHashMap<>());
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertNotNull(response);
    assertNotNull(response.getVectors());
    assertTrue(response.getVectors().isEmpty());
    assertNull(response.getNextToken());
  }

  @Test
  void listVectors_withStorageError_continuesWithoutData() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.getVectorData(1L)).thenThrow(new RuntimeException("Storage error"));
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, true, false, null, null);
    
    assertEquals(1, response.getVectors().size());
    assertNull(response.getVectors().get(0).getData());
  }

  @Test
  void listVectors_sortsByVectorId() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vectorC", createVectorMetadata("vectorC", 3L));
    vectorObjects.put("vectorA", createVectorMetadata("vectorA", 1L));
    vectorObjects.put("vectorB", createVectorMetadata("vectorB", 2L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", null, null, false, false, null, null);
    
    assertEquals(3, response.getVectors().size());
    assertEquals("vectorA", response.getVectors().get(0).getKey());
    assertEquals("vectorB", response.getVectors().get(1).getKey());
    assertEquals("vectorC", response.getVectors().get(2).getKey());
  }

  @Test
  void listVectors_withNegativeMaxResults_usesDefault() {
    TestListVectorsService service = new TestListVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);
    
    Map<String, VectorObjectMetadata> vectorObjects = new ConcurrentHashMap<>();
    vectorObjects.put("vector1", createVectorMetadata("vector1", 1L));
    indexMetadata.setVectorObjects(vectorObjects);
    
    service.bucketMap.put("bucket", bucketMetadata);
    
    ListVectorsResponse response = service.listVectors("bucket", "index", -5, null, false, false, null, null);
    
    assertEquals(1, response.getVectors().size());
  }

  private VectorObjectMetadata createVectorMetadata(String vectorId, Long storageId) {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setVectorId(vectorId);
    metadata.setStorageId(storageId);
    return metadata;
  }

  // Test implementation class
  private static class TestListVectorsService implements ListVectorsService {
    private final LocalS3VectorsMetadata mockMetadata = mock(LocalS3VectorsMetadata.class);
    final VectorStorage mockVectorStorage = mock(VectorStorage.class);
    final Map<String, VectorBucketMetadata> bucketMap = new HashMap<>();

    public TestListVectorsService() {
      when(mockMetadata.getVectorBucketMetadataMap()).thenReturn(bucketMap);
    }

    @Override
    public LocalS3VectorsMetadata metadata() {
      return mockMetadata;
    }

    @Override
    public VectorStorage vectorStorage() {
      return mockVectorStorage;
    }
  }
}
