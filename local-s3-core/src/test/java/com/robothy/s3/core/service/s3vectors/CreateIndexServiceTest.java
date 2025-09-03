package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.VectorDataType;
import com.robothy.s3.datatypes.s3vectors.response.CreateIndexResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CreateIndexService.
 */
class CreateIndexServiceTest {

  @Test
  void createIndex_withValidParameters_createsIndex() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null);

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    
    VectorIndexMetadata metadata = indexMetadata.get();
    assertEquals("test-index", metadata.getIndexName());
    assertEquals(128, metadata.getDimension());
    assertEquals(VectorDataType.FLOAT32, metadata.getDataType());
    assertEquals(DistanceMetric.EUCLIDEAN, metadata.getDistanceMetric());
    assertTrue(metadata.getCreationDate() > 0);
    assertTrue(metadata.isActive());
  }

  @Test
  void createIndex_withCosineDistanceMetric_createsIndex() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 256, DistanceMetric.COSINE, null);

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    assertEquals(DistanceMetric.COSINE, indexMetadata.get().getDistanceMetric());
  }

  @Test
  void createIndex_withNonFilterableMetadataKeys_createsIndexWithKeys() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);
    List<String> nonFilterableKeys = Arrays.asList("key1", "key2", "key3");

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 512, DistanceMetric.EUCLIDEAN, nonFilterableKeys);

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    
    List<String> storedKeys = indexMetadata.get().getNonFilterableMetadataKeys();
    assertNotNull(storedKeys);
    assertEquals(3, storedKeys.size());
    assertTrue(storedKeys.contains("key1"));
    assertTrue(storedKeys.contains("key2"));
    assertTrue(storedKeys.contains("key3"));
  }

  @Test
  void createIndex_withEmptyNonFilterableMetadataKeys_createsIndexWithoutKeys() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 64, DistanceMetric.EUCLIDEAN, Arrays.asList());

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    assertNull(indexMetadata.get().getNonFilterableMetadataKeys());
  }

  @Test
  void createIndex_withMinimumDimension_createsIndex() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 1, DistanceMetric.EUCLIDEAN, null);

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    assertEquals(1, indexMetadata.get().getDimension());
  }

  @Test
  void createIndex_withMaximumDimension_createsIndex() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    CreateIndexResponse response = service.createIndex(
        "test-bucket", "test-index", VectorDataType.FLOAT32, 4096, DistanceMetric.EUCLIDEAN, null);

    assertNotNull(response);
    Optional<VectorIndexMetadata> indexMetadata = bucketMetadata.getIndexMetadata("test-index");
    assertTrue(indexMetadata.isPresent());
    assertEquals(4096, indexMetadata.get().getDimension());
  }

  @Test
  void createIndex_withBlankIndexName_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", "", VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void createIndex_withNullIndexName_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", null, VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void createIndex_withWhitespaceIndexName_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", "   ", VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void createIndex_withZeroDimension_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        service.createIndex("test-bucket", "test-index", VectorDataType.FLOAT32, 0, DistanceMetric.EUCLIDEAN, null));

    assertEquals("Vector dimension must be between 1 and 4096, got: 0", exception.getMessage());
  }

  @Test
  void createIndex_withNegativeDimension_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        service.createIndex("test-bucket", "test-index", VectorDataType.FLOAT32, -1, DistanceMetric.EUCLIDEAN, null));

    assertEquals("Vector dimension must be between 1 and 4096, got: -1", exception.getMessage());
  }

  @Test
  void createIndex_withExcessiveDimension_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        service.createIndex("test-bucket", "test-index", VectorDataType.FLOAT32, 4097, DistanceMetric.EUCLIDEAN, null));

    assertEquals("Vector dimension must be between 1 and 4096, got: 4097", exception.getMessage());
  }

  @Test
  void createIndex_withNullDataType_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", "test-index", null, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Data type is required", exception.getMessage());
  }

  @Test
  void createIndex_withNullDistanceMetric_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", "test-index", VectorDataType.FLOAT32, 128, null, null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Distance metric is required", exception.getMessage());
  }

  @Test
  void createIndex_withNonExistentBucket_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("non-existent-bucket", "test-index", VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.NOT_FOUND, exception.getErrorType());
    assertEquals("The specified vector bucket could not be found", exception.getMessage());
  }

  @Test
  void createIndex_withExistingIndexName_throwsException() {
    CreateIndexServiceImpl service = new CreateIndexServiceImpl();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("test-bucket");
    
    // Create existing index
    VectorIndexMetadata existingIndex = new VectorIndexMetadata();
    existingIndex.setIndexName("existing-index");
    bucketMetadata.putIndexMetadata("existing-index", existingIndex);
    
    service.mockMetadata.getVectorBucketMetadataMap().put("test-bucket", bucketMetadata);

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, () ->
        service.createIndex("test-bucket", "existing-index", VectorDataType.FLOAT32, 128, DistanceMetric.EUCLIDEAN, null));

    assertEquals(LocalS3VectorErrorType.INDEX_ALREADY_EXISTS, exception.getErrorType());
    assertEquals("The index already exists", exception.getMessage());
  }

  /**
   * Test implementation of CreateIndexService for testing purposes.
   */
  private static class CreateIndexServiceImpl implements CreateIndexService {
    private final LocalS3VectorsMetadata mockMetadata = mock(LocalS3VectorsMetadata.class);
    private final Map<String, VectorBucketMetadata> bucketMap = new HashMap<>();

    public CreateIndexServiceImpl() {
      when(mockMetadata.getVectorBucketMetadataMap()).thenReturn(bucketMap);
    }

    @Override
    public LocalS3VectorsMetadata metadata() {
      return mockMetadata;
    }
  }
}
