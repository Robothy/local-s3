package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import com.robothy.s3.datatypes.s3vectors.response.DeleteVectorsResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class DeleteVectorsServiceTest {

  @Test
  void deleteVectors_withValidRequest_deletesVectorsSuccessfully() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(vectorMetadata);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(true);

    List<String> keys = List.of("vector1");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNotNull(response.getDeletedVectorKeys());
    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals("vector1", response.getDeletedVectorKeys().get(0));
    assertNull(response.getErrorVectorKeys());

    verify(service.mockVectorStorage).deleteVectorData(1L);
  }

  @Test
  void deleteVectors_withNonExistentBucket_throwsException() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();

    List<String> keys = List.of("vector1");

    assertThrows(LocalS3VectorException.class,
        () -> service.deleteVectors("nonexistent", "index", keys));
  }

  @Test
  void deleteVectors_withNonExistentIndex_throwsException() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    service.bucketMap.put("bucket", bucketMetadata);

    List<String> keys = List.of("vector1");

    assertThrows(LocalS3VectorException.class,
        () -> service.deleteVectors("bucket", "nonexistent", keys));
  }

  @Test
  void deleteVectors_withNullKeys_throwsException() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();

    assertThrows(LocalS3VectorException.class,
        () -> service.deleteVectors("bucket", "index", null));
  }

  @Test
  void deleteVectors_withEmptyKeys_throwsException() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();

    List<String> emptyKeys = List.of();

    assertThrows(LocalS3VectorException.class,
        () -> service.deleteVectors("bucket", "index", emptyKeys));
  }

  @Test
  void deleteVectors_withTooManyKeys_throwsException() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();

    List<String> tooManyKeys = IntStream.range(0, 501)
        .mapToObj(i -> "vector" + i)
        .toList();

    assertThrows(LocalS3VectorException.class,
        () -> service.deleteVectors("bucket", "index", tooManyKeys));
  }

  @Test
  void deleteVectors_withNonExistentVector_addsToErrorList() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    service.bucketMap.put("bucket", bucketMetadata);

    List<String> keys = List.of("nonexistent");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNull(response.getDeletedVectorKeys());
    assertNotNull(response.getErrorVectorKeys());
    assertEquals(1, response.getErrorVectorKeys().size());
    assertEquals("nonexistent", response.getErrorVectorKeys().get(0));
  }

  @Test
  void deleteVectors_withMixedResults_returnsBothListsCorrectly() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata existingVector = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(existingVector);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(true);

    List<String> keys = List.of("vector1", "nonexistent");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNotNull(response.getDeletedVectorKeys());
    assertNotNull(response.getErrorVectorKeys());

    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals("vector1", response.getDeletedVectorKeys().get(0));

    assertEquals(1, response.getErrorVectorKeys().size());
    assertEquals("nonexistent", response.getErrorVectorKeys().get(0));
  }

  @Test
  void deleteVectors_withStorageFailure_continuesWithMetadataCleanup() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(vectorMetadata);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(false); // Storage failure

    List<String> keys = List.of("vector1");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals("vector1", response.getDeletedVectorKeys().get(0));

    // Should still remove from metadata even if storage deletion fails
    assertNull(indexMetadata.getVectorObject("vector1"));
  }

  @Test
  void deleteVectors_withNullStorageId_continuesWithMetadataCleanup() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", null); // No storage ID
    indexMetadata.addVectorObject(vectorMetadata);

    service.bucketMap.put("bucket", bucketMetadata);

    List<String> keys = List.of("vector1");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals("vector1", response.getDeletedVectorKeys().get(0));

    verify(service.mockVectorStorage, never()).deleteVectorData(anyLong());
    assertNull(indexMetadata.getVectorObject("vector1"));
  }

  @Test
  void deleteVectors_withMultipleVectors_processesAll() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vector1 = createVectorMetadata("vector1", 1L);
    VectorObjectMetadata vector2 = createVectorMetadata("vector2", 2L);
    indexMetadata.addVectorObject(vector1);
    indexMetadata.addVectorObject(vector2);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(anyLong())).thenReturn(true);

    List<String> keys = List.of("vector1", "vector2");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(2, response.getDeletedVectorKeys().size());
    assertTrue(response.getDeletedVectorKeys().contains("vector1"));
    assertTrue(response.getDeletedVectorKeys().contains("vector2"));

    verify(service.mockVectorStorage).deleteVectorData(1L);
    verify(service.mockVectorStorage).deleteVectorData(2L);
    assertNull(indexMetadata.getVectorObject("vector1"));
    assertNull(indexMetadata.getVectorObject("vector2"));
  }

  @Test
  void deleteVectors_withExactly500Keys_succeeds() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    // Setup 500 vectors
    List<String> keys = new ArrayList<>();
    for (int i = 1; i <= 500; i++) {
      String key = "vector" + i;
      keys.add(key);
      VectorObjectMetadata vector = createVectorMetadata(key, (long) i);
      indexMetadata.addVectorObject(vector);
    }

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(anyLong())).thenReturn(true);

    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(500, response.getDeletedVectorKeys().size());
    assertNull(response.getErrorVectorKeys());
  }

  @Test
  void deleteVectors_withStorageException_addsToErrorList() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(vectorMetadata);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenThrow(new RuntimeException("Storage error"));

    List<String> keys = List.of("vector1");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNull(response.getDeletedVectorKeys());
    assertNotNull(response.getErrorVectorKeys());
    assertEquals(1, response.getErrorVectorKeys().size());
    assertEquals("vector1", response.getErrorVectorKeys().get(0));
  }

  @Test
  void deleteVectors_withMetadataException_addsToErrorList() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = spy(new VectorIndexMetadata());
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vectorMetadata = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(vectorMetadata);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(true);
    doThrow(new RuntimeException("Metadata error")).when(indexMetadata).removeVectorObject("vector1");

    List<String> keys = List.of("vector1");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNull(response.getDeletedVectorKeys());
    assertNotNull(response.getErrorVectorKeys());
    assertEquals(1, response.getErrorVectorKeys().size());
    assertEquals("vector1", response.getErrorVectorKeys().get(0));
  }

  @Test
  void deleteVectors_withPartialFailures_handlesCorrectly() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vector1 = createVectorMetadata("vector1", 1L);
    VectorObjectMetadata vector2 = createVectorMetadata("vector2", 2L);
    indexMetadata.addVectorObject(vector1);
    indexMetadata.addVectorObject(vector2);

    service.bucketMap.put("bucket", bucketMetadata);

    // vector1 succeeds, vector2 fails in storage, nonexistent fails in metadata
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(true);
    when(service.mockVectorStorage.deleteVectorData(2L)).thenThrow(new RuntimeException("Storage error"));

    List<String> keys = List.of("vector1", "vector2", "nonexistent");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals("vector1", response.getDeletedVectorKeys().get(0));

    assertEquals(2, response.getErrorVectorKeys().size());
    assertTrue(response.getErrorVectorKeys().contains("vector2"));
    assertTrue(response.getErrorVectorKeys().contains("nonexistent"));
  }

  @Test
  void deleteVectors_withAllSuccessfulDeletions_returnsOnlyDeletedList() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vector1 = createVectorMetadata("vector1", 1L);
    VectorObjectMetadata vector2 = createVectorMetadata("vector2", 2L);
    indexMetadata.addVectorObject(vector1);
    indexMetadata.addVectorObject(vector2);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(anyLong())).thenReturn(true);

    List<String> keys = List.of("vector1", "vector2");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(2, response.getDeletedVectorKeys().size());
    assertNull(response.getErrorVectorKeys());
  }

  @Test
  void deleteVectors_withAllFailedDeletions_returnsOnlyErrorList() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    service.bucketMap.put("bucket", bucketMetadata);

    List<String> keys = List.of("nonexistent1", "nonexistent2");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertNull(response.getDeletedVectorKeys());
    assertEquals(2, response.getErrorVectorKeys().size());
    assertTrue(response.getErrorVectorKeys().contains("nonexistent1"));
    assertTrue(response.getErrorVectorKeys().contains("nonexistent2"));
  }

  @Test
  void deleteVectors_maintainsDeletionOrder() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vector1 = createVectorMetadata("vector1", 1L);
    VectorObjectMetadata vector2 = createVectorMetadata("vector2", 2L);
    VectorObjectMetadata vector3 = createVectorMetadata("vector3", 3L);
    indexMetadata.addVectorObject(vector1);
    indexMetadata.addVectorObject(vector2);
    indexMetadata.addVectorObject(vector3);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(anyLong())).thenReturn(true);

    List<String> keys = List.of("vector3", "vector1", "vector2");
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(3, response.getDeletedVectorKeys().size());
    assertEquals("vector3", response.getDeletedVectorKeys().get(0));
    assertEquals("vector1", response.getDeletedVectorKeys().get(1));
    assertEquals("vector2", response.getDeletedVectorKeys().get(2));
  }

  @Test
  void deleteVectors_withDuplicateKeys_processesEach() {
    TestDeleteVectorsService service = new TestDeleteVectorsService();
    VectorBucketMetadata bucketMetadata = new VectorBucketMetadata();
    bucketMetadata.setVectorBucketName("bucket");
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName("index");
    bucketMetadata.putIndexMetadata("index", indexMetadata);

    VectorObjectMetadata vector1 = createVectorMetadata("vector1", 1L);
    indexMetadata.addVectorObject(vector1);

    service.bucketMap.put("bucket", bucketMetadata);
    when(service.mockVectorStorage.deleteVectorData(1L)).thenReturn(true);

    List<String> keys = List.of("vector1", "vector1"); // Duplicate key
    DeleteVectorsResponse response = service.deleteVectors("bucket", "index", keys);

    assertNotNull(response);
    assertEquals(1, response.getDeletedVectorKeys().size());
    assertEquals(1, response.getErrorVectorKeys().size());
    assertTrue(response.getDeletedVectorKeys().contains("vector1"));
    assertTrue(response.getErrorVectorKeys().contains("vector1"));
  }

  private VectorObjectMetadata createVectorMetadata(String vectorId, Long storageId) {
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setVectorId(vectorId);
    metadata.setStorageId(storageId);
    return metadata;
  }

  // Test implementation class
  private static class TestDeleteVectorsService implements DeleteVectorsService {
    private final LocalS3VectorsMetadata mockMetadata = mock(LocalS3VectorsMetadata.class);
    final VectorStorage mockVectorStorage = mock(VectorStorage.class);
    final Map<String, VectorBucketMetadata> bucketMap = new HashMap<>();

    public TestDeleteVectorsService() {
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
