package com.robothy.s3.core.service.s3vectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import java.util.List;
import java.util.function.Function;

/**
 * Vector search engine interface for S3 Vectors similarity search operations.
 * Provides distance-based vector similarity calculation and TopK result selection.
 * <p>
 * The search engine works with the new separated architecture where vector metadata
 * and vector data are stored separately.
 */
public interface VectorSearchEngine {

  /**
   * Create a basic vector search engine implementation.
   *
   * @return a new basic vector search engine instance
   */
  static VectorSearchEngine createBasic() {
    return new BasicVectorSearchEngine();
  }

  /**
   * Calculate the distance between two vectors using the specified metric.
   *
   * @param vector1 the first vector
   * @param vector2 the second vector
   * @param metric  the distance metric to use
   * @return the calculated distance between the vectors
   * @throws IllegalArgumentException if vectors have different dimensions or are invalid
   */
  double calculateDistance(float[] vector1, float[] vector2, DistanceMetric metric);

  /**
   * Find the K nearest vectors to the query vector from a collection of vectors.
   *
   * @param queryVector      the query vector to search for
   * @param candidateVectors the collection of vector metadata to search in
   * @param vectorDataLookup function to retrieve vector data by storage ID
   * @param distanceMetric   the distance metric to use for calculations
   * @param k                the number of nearest neighbors to return
   * @param metadataFilter   optional metadata filter to apply before distance calculation (JSON format)
   * @return list of search results ordered by distance (closest first)
   * @throws IllegalArgumentException if k is invalid or query vector is null
   */
  List<VectorSearchResult> findNearestVectors(
      float[] queryVector,
      List<VectorObjectMetadata> candidateVectors,
      Function<Long, float[]> vectorDataLookup,
      DistanceMetric distanceMetric,
      int k,
      JsonNode metadataFilter
  );

  /**
   * Convenience method that uses VectorStorage for data lookup.
   *
   * @param queryVector      the query vector to search for
   * @param candidateVectors the collection of vector metadata to search in
   * @param vectorStorage    the vector storage instance for data retrieval
   * @param distanceMetric   the distance metric to use for calculations
   * @param k                the number of nearest neighbors to return
   * @param metadataFilter   optional metadata filter to apply before distance calculation (JSON format)
   * @return list of search results ordered by distance (closest first)
   */
  default List<VectorSearchResult> findNearestVectors(
      float[] queryVector,
      List<VectorObjectMetadata> candidateVectors,
      VectorStorage vectorStorage,
      DistanceMetric distanceMetric,
      int k,
      JsonNode metadataFilter) {
    return findNearestVectors(queryVector, candidateVectors, vectorStorage::getVectorData,
        distanceMetric, k, metadataFilter);
  }

  /**
   * Represents a vector search result with distance information.
   */
  record VectorSearchResult(VectorObjectMetadata vectorMetadata, double distance) {

    @Override
    public String toString() {
      return String.format("VectorSearchResult{vectorId='%s', distance=%.6f}",
          vectorMetadata.getVectorId(), distance);
    }
  }
}