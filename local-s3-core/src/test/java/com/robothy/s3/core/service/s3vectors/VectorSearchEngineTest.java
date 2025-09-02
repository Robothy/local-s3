package com.robothy.s3.core.service.s3vectors;

import static org.junit.jupiter.api.Assertions.*;

import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VectorSearchEngine} implementations.
 */
class VectorSearchEngineTest {

  private VectorSearchEngine searchEngine;
  private VectorStorage vectorStorage;

  @BeforeEach
  void setUp() {
    searchEngine = VectorSearchEngine.createBasic();
    vectorStorage = VectorStorage.createInMemory();
  }

  @Test
  void testEuclideanDistance() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {4.0f, 5.0f, 6.0f};

    double distance = searchEngine.calculateDistance(vector1, vector2, DistanceMetric.EUCLIDEAN);

    // Expected: sqrt((4-1)^2 + (5-2)^2 + (6-3)^2) = sqrt(9 + 9 + 9) = sqrt(27) ≈ 5.196
    assertEquals(5.196152422706632, distance, 0.0001);
  }

  @Test
  void testEuclideanDistanceIdenticalVectors() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {1.0f, 2.0f, 3.0f};

    double distance = searchEngine.calculateDistance(vector1, vector2, DistanceMetric.EUCLIDEAN);
    assertEquals(0.0, distance, 0.0001);
  }


  //@Test
  void testCosineDistance() {
    float[] vector1Data = new float[] {1.1f, 1.2f, 1.0f, 1.2f, 1.3f};
    float[] vector2Data = new float[] {2.1f, 2.2f, 2.0f, 2.2f, 2.3f};
    float[] vector3Data = new float[] {3.1f, 3.2f, 3.0f, 3.2f, 3.3f};

    float[] queryVector = new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    assertEquals(0.003602028f, searchEngine.calculateDistance(queryVector, vector1Data, DistanceMetric.COSINE), 0.0001);
    assertEquals(0.0016186237f, searchEngine.calculateDistance(queryVector, vector2Data, DistanceMetric.COSINE), 0.0001);
    assertEquals(7.5495243E-4f, searchEngine.calculateDistance(queryVector, vector3Data, DistanceMetric.COSINE), 0.0001);
  }

  @Test
  void testCosineDistanceIdenticalVectors() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {1.0f, 2.0f, 3.0f};

    double distance = searchEngine.calculateDistance(vector1, vector2, DistanceMetric.COSINE);

    // Identical vectors have cosine similarity = 1, distance = 1 - 1 = 0
    assertEquals(0.0, distance, 0.0001);
  }

  @Test
  void testCosineDistanceParallelVectors() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {2.0f, 4.0f, 6.0f}; // Same direction, double magnitude

    double distance = searchEngine.calculateDistance(vector1, vector2, DistanceMetric.COSINE);

    // Parallel vectors have cosine similarity = 1, distance = 1 - 1 = 0
    assertEquals(0.0, distance, 0.0001);
  }

  @Test
  void testCosineDistanceZeroVector() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {0.0f, 0.0f, 0.0f};

    double distance = searchEngine.calculateDistance(vector1, vector2, DistanceMetric.COSINE);

    // Zero vector case should return distance = 1.0 (maximum distance)
    assertEquals(1.0, distance, 0.0001);
  }

  @Test
  void testDistanceCalculationValidation() {
    float[] vector1 = {1.0f, 2.0f, 3.0f};
    float[] vector2 = {1.0f, 2.0f}; // Different dimension

    // Different dimensions should throw exception
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.calculateDistance(vector1, vector2, DistanceMetric.EUCLIDEAN));

    // Null vectors should throw exception
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.calculateDistance(null, vector2, DistanceMetric.EUCLIDEAN));

    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.calculateDistance(vector1, null, DistanceMetric.EUCLIDEAN));

    // Empty vectors should throw exception
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.calculateDistance(new float[0], new float[0], DistanceMetric.EUCLIDEAN));
  }

  @Test
  void testFindNearestVectors() {
    // Store vector data and get storage IDs
    Long storageId1 = vectorStorage.putVectorData(new float[] {1.0f, 0.0f});
    Long storageId2 = vectorStorage.putVectorData(new float[] {0.0f, 1.0f});
    Long storageId3 = vectorStorage.putVectorData(new float[] {1.0f, 1.0f});

    // Create test vector metadata
    VectorObjectMetadata vec1 = new VectorObjectMetadata("vec1", 2, storageId1, null);
    VectorObjectMetadata vec2 = new VectorObjectMetadata("vec2", 2, storageId2, null);
    VectorObjectMetadata vec3 = new VectorObjectMetadata("vec3", 2, storageId3, null);

    List<VectorObjectMetadata> candidates = List.of(vec1, vec2, vec3);
    float[] queryVector = {1.0f, 0.0f}; // Should be closest to vec1, then vec3, then vec2

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 2, null
    );

    assertEquals(2, results.size());
    assertEquals("vec1", results.get(0).vectorMetadata().getVectorId());
    assertEquals(0.0, results.get(0).distance(), 0.0001);
    assertEquals("vec3", results.get(1).vectorMetadata().getVectorId());
    assertEquals(1.0, results.get(1).distance(), 0.0001);
  }

  @Test
  void testFindNearestVectorsValidation() {
    List<VectorObjectMetadata> candidates = List.of();

    // Null query vector
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.findNearestVectors(null, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 1, null));

    // Invalid k
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.findNearestVectors(new float[] {1.0f}, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 0, null));

    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.findNearestVectors(new float[] {1.0f}, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, -1, null));

    // Null vector lookup function
    assertThrows(IllegalArgumentException.class, () ->
        searchEngine.findNearestVectors(new float[] {1.0f}, candidates, (Function<Long, float[]>) null, DistanceMetric.EUCLIDEAN,
            1, null));
  }

  @Test
  void testFindNearestVectorsEmptyCandidates() {
    List<VectorObjectMetadata> candidates = List.of();
    float[] queryVector = {1.0f, 0.0f};

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 5, null
    );

    assertTrue(results.isEmpty());
  }

  @Test
  void testFindNearestVectorsDimensionMismatch() {
    Long storageId1 = vectorStorage.putVectorData(new float[] {1.0f, 0.0f});
    Long storageId2 = vectorStorage.putVectorData(new float[] {1.0f, 0.0f, 1.0f});

    VectorObjectMetadata vec1 = new VectorObjectMetadata("vec1", 2, storageId1, null);
    VectorObjectMetadata vec2 = new VectorObjectMetadata("vec2", 3, storageId2, null); // Different dimension

    List<VectorObjectMetadata> candidates = List.of(vec1, vec2);
    float[] queryVector = {1.0f, 0.0f};

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 5, null
    );

    // Should only include vectors with matching dimensions
    assertEquals(1, results.size());
    assertEquals("vec1", results.get(0).vectorMetadata().getVectorId());
  }

  @Test
  void testTopKLimiting() {
    // Create 5 test vectors at different distances
    Long storageId1 = vectorStorage.putVectorData(new float[] {1.0f, 0.0f});
    Long storageId2 = vectorStorage.putVectorData(new float[] {2.0f, 0.0f});
    Long storageId3 = vectorStorage.putVectorData(new float[] {3.0f, 0.0f});
    Long storageId4 = vectorStorage.putVectorData(new float[] {4.0f, 0.0f});
    Long storageId5 = vectorStorage.putVectorData(new float[] {5.0f, 0.0f});

    VectorObjectMetadata vec1 = new VectorObjectMetadata("vec1", 2, storageId1, null);
    VectorObjectMetadata vec2 = new VectorObjectMetadata("vec2", 2, storageId2, null);
    VectorObjectMetadata vec3 = new VectorObjectMetadata("vec3", 2, storageId3, null);
    VectorObjectMetadata vec4 = new VectorObjectMetadata("vec4", 2, storageId4, null);
    VectorObjectMetadata vec5 = new VectorObjectMetadata("vec5", 2, storageId5, null);

    List<VectorObjectMetadata> candidates = List.of(vec1, vec2, vec3, vec4, vec5);
    float[] queryVector = {0.0f, 0.0f}; // Should find vec1 closest, then vec2, etc.

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 3, null
    );

    // Should return only top 3 results
    assertEquals(3, results.size());
    assertEquals("vec1", results.get(0).vectorMetadata().getVectorId());
    assertEquals("vec2", results.get(1).vectorMetadata().getVectorId());
    assertEquals("vec3", results.get(2).vectorMetadata().getVectorId());

    // Results should be ordered by distance
    assertTrue(results.get(0).distance() <= results.get(1).distance());
    assertTrue(results.get(1).distance() <= results.get(2).distance());
  }

  @Test
  void testMissingVectorData() {
    // Create metadata pointing to non-existent storage ID
    VectorObjectMetadata vec1 = new VectorObjectMetadata("vec1", 2, 999L, null);

    List<VectorObjectMetadata> candidates = List.of(vec1);
    float[] queryVector = {1.0f, 0.0f};

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 5, null
    );

    // Should skip vectors with missing data
    assertTrue(results.isEmpty());
  }

  @Test
  void testMissingStorageId() {
    // Create metadata with null storage ID
    VectorObjectMetadata vec1 = new VectorObjectMetadata("vec1", 2, null, null);

    List<VectorObjectMetadata> candidates = List.of(vec1);
    float[] queryVector = {1.0f, 0.0f};

    List<VectorSearchEngine.VectorSearchResult> results = searchEngine.findNearestVectors(
        queryVector, candidates, vectorStorage, DistanceMetric.EUCLIDEAN, 5, null
    );

    // Should skip vectors with null storage ID
    assertTrue(results.isEmpty());
  }
}
