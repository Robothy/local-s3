package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Basic implementation of {@linkplain VectorSearchEngine} using brute-force search.
 * This implementation calculates distances between the query vector and all candidate vectors,
 * then returns the K nearest neighbors using a heap-based approach for efficiency.
 */
@Slf4j
class BasicVectorSearchEngine implements VectorSearchEngine {

  @Override
  public double calculateDistance(float[] vector1, float[] vector2, DistanceMetric metric) {

    if (vector1 == null || vector2 == null) {
      throw new IllegalArgumentException("Vectors cannot be null");
    }
    if (vector1.length != vector2.length) {
      throw new IllegalArgumentException(
          String.format("Vector dimensions must match: %d vs %d", vector1.length, vector2.length));
    }
    if (vector1.length == 0) {
      throw new IllegalArgumentException("Vectors cannot be empty");
    }

    return switch (metric) {
      case EUCLIDEAN -> calculateEuclideanDistance(vector1, vector2);
      case COSINE -> calculateCosineDistance(vector1, vector2);
    };
  }

  @Override
  public List<VectorSearchResult> findNearestVectors(
      float[] queryVector,
      List<VectorObjectMetadata> candidateVectors,
      Function<Long, float[]> vectorDataLookup,
      DistanceMetric distanceMetric,
      int k,
      com.fasterxml.jackson.databind.JsonNode metadataFilter) {

    if (queryVector == null) {
      throw new IllegalArgumentException("Query vector cannot be null");
    }
    if (vectorDataLookup == null) {
      throw new IllegalArgumentException("Vector data lookup function cannot be null");
    }
    if (k <= 0) {
      throw new IllegalArgumentException("k must be positive, got: " + k);
    }
    if (candidateVectors == null || candidateVectors.isEmpty()) {
      return List.of();
    }

    log.debug("Searching for {} nearest vectors from {} candidates using {} metric",
        k, candidateVectors.size(), distanceMetric);

    // Apply metadata filters first to reduce computation
    List<VectorObjectMetadata> filteredVectors = MetadataFilter.applyFilter(candidateVectors, metadataFilter);

    if (filteredVectors.isEmpty()) {
      return List.of();
    }

    // Use a max-heap to maintain the K nearest vectors efficiently
    PriorityQueue<VectorSearchResult> maxHeap = new PriorityQueue<>(
        Comparator.comparing(VectorSearchResult::distance).reversed()
    );

    for (VectorObjectMetadata vectorMetadata : filteredVectors) {
      try {
        // Look up vector data using the storage ID
        Long storageId = vectorMetadata.getStorageId();
        if (storageId == null) {
          log.warn("Skipping vector {} due to missing storage ID", vectorMetadata.getVectorId());
          continue;
        }

        float[] candidateVector = vectorDataLookup.apply(storageId);
        if (candidateVector == null) {
          log.warn("Skipping vector {} due to missing vector data for storage ID {}",
              vectorMetadata.getVectorId(), storageId);
          continue;
        }

        // Validate dimensions
        if (candidateVector.length != queryVector.length) {
          log.warn("Skipping vector {} due to dimension mismatch: expected {}, got {}",
              vectorMetadata.getVectorId(), queryVector.length, candidateVector.length);
          continue;
        }

        double distance = calculateDistance(queryVector, candidateVector, distanceMetric);
        VectorSearchResult result = new VectorSearchResult(vectorMetadata, distance);

        if (maxHeap.size() < k) {
          maxHeap.offer(result);
        } else if (distance < maxHeap.peek().distance()) {
          maxHeap.poll();
          maxHeap.offer(result);
        }
      } catch (Exception e) {
        log.warn("Error processing vector {}: {}", vectorMetadata.getVectorId(), e.getMessage());
      }
    }

    // Convert heap to sorted list (closest first)
    List<VectorSearchResult> results = maxHeap.stream()
        .sorted(Comparator.comparing(VectorSearchResult::distance))
        .collect(Collectors.toList());

    log.debug("Found {} nearest vectors", results.size());
    return results;
  }

  /**
   * Calculate Euclidean distance: √(∑(ai - bi)²)
   */
  private double calculateEuclideanDistance(float[] vector1, float[] vector2) {
    double sumSquaredDiffs = 0.0;

    for (int i = 0; i < vector1.length; i++) {
      double diff = vector1[i] - vector2[i];
      sumSquaredDiffs += diff * diff;
    }

    return Math.sqrt(sumSquaredDiffs);
  }

  /**
   * Calculate Cosine distance: 1 - (A·B)/(||A|| × ||B||)
   * Returns a value between 0 and 2, where 0 means identical direction.
   */
  private double calculateCosineDistance(float[] vector1, float[] vector2) {
    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    for (int i = 0; i < vector1.length; i++) {
      dotProduct += vector1[i] * vector2[i];
      normA += vector1[i] * vector1[i];
      normB += vector2[i] * vector2[i];
    }

    normA = Math.sqrt(normA);
    normB = Math.sqrt(normB);

    // Handle zero vectors (avoid division by zero)
    if (normA == 0.0 || normB == 0.0) {
      return 1.0; // Maximum cosine distance for zero vectors
    }

    double cosineSimilarity = dotProduct / (normA * normB);
    // Clamp to [-1, 1] to handle floating point precision issues
    cosineSimilarity = Math.max(-1.0, Math.min(1.0, cosineSimilarity));

    return 1.0 - cosineSimilarity;
  }

}
