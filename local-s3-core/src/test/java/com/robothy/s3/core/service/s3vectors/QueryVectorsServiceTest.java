package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import com.robothy.s3.datatypes.s3vectors.response.QueryOutputVector;
import com.robothy.s3.datatypes.s3vectors.response.QueryVectorsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for QueryVectorsService.
 */
class QueryVectorsServiceTest {

  @Test
  void queryVectors_withValidInputs_returnsResults() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    addVectorToIndex(indexMetadata, "vector1", 1L, new float[]{1.0f, 2.0f, 3.0f}, null);
    addVectorToIndex(indexMetadata, "vector2", 2L, new float[]{4.0f, 5.0f, 6.0f}, null);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    when(service.vectorStorage.getVectorData(1L)).thenReturn(new float[]{1.0f, 2.0f, 3.0f});
    when(service.vectorStorage.getVectorData(2L)).thenReturn(new float[]{4.0f, 5.0f, 6.0f});
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.1f, 2.1f, 3.1f});

    QueryVectorsResponse response = service.queryVectors("test-bucket", "test-index", 
        queryVector, 2, true, false, null);

    assertNotNull(response);
    assertEquals(2, response.getVectors().size());
    
    QueryOutputVector firstResult = response.getVectors().get(0);
    assertEquals("vector1", firstResult.getKey());
    assertNotNull(firstResult.getDistance());
    assertTrue(firstResult.getDistance() < 1.0); // Should be close to query vector
  }

  @Test
  void queryVectors_withReturnMetadata_includesMetadata() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 2, DistanceMetric.COSINE);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode metadata = mapper.createObjectNode().put("category", "test");
    addVectorToIndex(indexMetadata, "vector1", 1L, new float[]{1.0f, 2.0f}, metadata);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    when(service.vectorStorage.getVectorData(1L)).thenReturn(new float[]{1.0f, 2.0f});
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f});

    QueryVectorsResponse response = service.queryVectors("test-bucket", "test-index", 
        queryVector, 1, false, true, null);

    assertNotNull(response);
    assertEquals(1, response.getVectors().size());
    
    QueryOutputVector result = response.getVectors().get(0);
    assertEquals("vector1", result.getKey());
    assertNotNull(result.getMetadata());
    assertEquals("test", result.getMetadata().get("category").asText());
    assertNull(result.getDistance()); // returnDistance was false
  }

  @Test
  void queryVectors_withEmptyIndex_returnsEmptyResponse() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f, 3.0f});

    QueryVectorsResponse response = service.queryVectors("test-bucket", "test-index", 
        queryVector, 1, true, true, null);

    assertNotNull(response);
    assertTrue(response.getVectors().isEmpty());
  }

  @Test
  void queryVectors_withNonExistentBucket_throwsException() {
    TestQueryVectorsService service = createTestService();
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f, 3.0f});

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("non-existent", "test-index", queryVector, 1, true, true, null));

    assertEquals(LocalS3VectorErrorType.NOT_FOUND, exception.getErrorType());
  }

  @Test
  void queryVectors_withNonExistentIndex_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f, 3.0f});

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "non-existent", queryVector, 1, true, true, null));

    assertEquals(LocalS3VectorErrorType.NOT_FOUND, exception.getErrorType());
  }

  @Test
  void queryVectors_withNullQueryVector_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "test-index", null, 1, true, true, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Query vector is required", exception.getMessage());
  }

  @Test
  void queryVectors_withEmptyQueryVector_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{});

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "test-index", queryVector, 1, true, true, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Query vector data cannot be empty", exception.getMessage());
  }

  @Test
  void queryVectors_withWrongDimension_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f}); // 2D instead of 3D

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "test-index", queryVector, 1, true, true, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Query vector dimension 2 does not match index dimension 3"));
  }

  @Test
  void queryVectors_withInvalidTopK_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f, 3.0f});

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "test-index", queryVector, 0, true, true, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("topK must be at least 1", exception.getMessage());
  }

  @Test
  void queryVectors_withNullTopK_throwsException() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 3, DistanceMetric.EUCLIDEAN);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f, 3.0f});

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> service.queryVectors("test-bucket", "test-index", queryVector, null, true, true, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("topK must be at least 1", exception.getMessage());
  }

  @Test
  void queryVectors_withTopKGreaterThanResults_returnsAllResults() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 2, DistanceMetric.EUCLIDEAN);
    addVectorToIndex(indexMetadata, "vector1", 1L, new float[]{1.0f, 2.0f}, null);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    when(service.vectorStorage.getVectorData(1L)).thenReturn(new float[]{1.0f, 2.0f});
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f});

    QueryVectorsResponse response = service.queryVectors("test-bucket", "test-index", 
        queryVector, 10, true, false, null); // topK=10 but only 1 vector

    assertNotNull(response);
    assertEquals(1, response.getVectors().size());
  }

  @Test
  void queryVectors_withFilter_appliesFilter() {
    TestQueryVectorsService service = createTestService();
    VectorBucketMetadata bucketMetadata = createVectorBucketMetadata("test-bucket");
    VectorIndexMetadata indexMetadata = createVectorIndexMetadata("test-index", 2, DistanceMetric.EUCLIDEAN);
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode metadata1 = mapper.createObjectNode().put("category", "A");
    JsonNode metadata2 = mapper.createObjectNode().put("category", "B");
    addVectorToIndex(indexMetadata, "vector1", 1L, new float[]{1.0f, 2.0f}, metadata1);
    addVectorToIndex(indexMetadata, "vector2", 2L, new float[]{3.0f, 4.0f}, metadata2);
    bucketMetadata.getIndexes().put("test-index", indexMetadata);
    service.metadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    
    when(service.vectorStorage.getVectorData(1L)).thenReturn(new float[]{1.0f, 2.0f});
    when(service.vectorStorage.getVectorData(2L)).thenReturn(new float[]{3.0f, 4.0f});
    
    PutInputVector.VectorData queryVector = createVectorData(new float[]{1.0f, 2.0f});
    JsonNode filter = mapper.createObjectNode().put("category", "A");

    QueryVectorsResponse response = service.queryVectors("test-bucket", "test-index", 
        queryVector, 2, false, true, filter);

    assertNotNull(response);
    assertEquals(1, response.getVectors().size()); // Only vector1 should match filter
    assertEquals("vector1", response.getVectors().get(0).getKey());
  }

  private TestQueryVectorsService createTestService() {
    LocalS3VectorsMetadata metadata = new LocalS3VectorsMetadata();
    VectorStorage vectorStorage = mock(VectorStorage.class);
    return new TestQueryVectorsService(metadata, vectorStorage);
  }

  private VectorBucketMetadata createVectorBucketMetadata(String bucketName) {
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName(bucketName);
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    return bucketMetadata;
  }

  private VectorIndexMetadata createVectorIndexMetadata(String indexName, int dimension, DistanceMetric metric) {
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName(indexName);
    indexMetadata.setDimension(dimension);
    indexMetadata.setDistanceMetric(metric);
    return indexMetadata;
  }

  private void addVectorToIndex(VectorIndexMetadata indexMetadata, String vectorId, Long storageId, 
                               float[] vectorData, JsonNode metadata) {
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata(vectorId, vectorData.length, storageId, metadata);
    indexMetadata.getVectorObjects().put(vectorId, vectorMetadata);
  }

  private PutInputVector.VectorData createVectorData(float[] values) {
    PutInputVector.VectorData vectorData = new PutInputVector.VectorData();
    vectorData.setValues(values);
    return vectorData;
  }

  private static class TestQueryVectorsService implements QueryVectorsService {
    private final LocalS3VectorsMetadata metadata;
    private final VectorStorage vectorStorage;

    public TestQueryVectorsService(LocalS3VectorsMetadata metadata, VectorStorage vectorStorage) {
      this.metadata = metadata;
      this.vectorStorage = vectorStorage;
    }

    @Override
    public LocalS3VectorsMetadata metadata() {
      return metadata;
    }

    @Override
    public VectorStorage vectorStorage() {
      return vectorStorage;
    }
  }
}
