package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.jupiter.LocalS3;
import io.github.robothy.s3.RealS3;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.DataType;
import software.amazon.awssdk.services.s3vectors.model.DistanceMetric;
import software.amazon.awssdk.services.s3vectors.model.IndexSummary;
import software.amazon.awssdk.services.s3vectors.model.ListIndexesResponse;

public class ListIndexesIntegrationTest {

  @LocalS3
  @Test
  void testListIndexes(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket-" + UUID.randomUUID();
    String indexName1 = "test-index-" + UUID.randomUUID();
    String indexName2 = "test-index-" + UUID.randomUUID();
    String indexName3 = "different-prefix-" + UUID.randomUUID();

    try {
      // Create vector bucket
      vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));

      // Test listing when no indexes exist
      ListIndexesResponse emptyResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName).maxResults(10));
      assertNotNull(emptyResponse);
      assertNotNull(emptyResponse.indexes());
      assertEquals(0, emptyResponse.indexes().size());

      // Create test indexes
      vectorsClient.createIndex(b -> b.vectorBucketName(bucketName)
          .indexName(indexName1)
          .dimension(5)
          .dataType(DataType.FLOAT32)
          .distanceMetric(DistanceMetric.COSINE));

      vectorsClient.createIndex(b -> b.vectorBucketName(bucketName)
          .indexName(indexName2)
          .dimension(10)
          .dataType(DataType.FLOAT32)
          .distanceMetric(DistanceMetric.EUCLIDEAN));

      vectorsClient.createIndex(b -> b.vectorBucketName(bucketName)
          .indexName(indexName3)
          .dimension(15)
          .dataType(DataType.FLOAT32)
          .distanceMetric(DistanceMetric.COSINE));

      // Test basic listing by bucket name
      ListIndexesResponse response = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName).maxResults(10));
      assertNotNull(response);
      assertNotNull(response.indexes());
      assertEquals(3, response.indexes().size());

      // Verify all created indexes are present
      List<String> indexNames = response.indexes().stream()
          .map(IndexSummary::indexName)
          .toList();
      assertTrue(indexNames.contains(indexName1));
      assertTrue(indexNames.contains(indexName2));
      assertTrue(indexNames.contains(indexName3));

      // Test prefix filtering
      ListIndexesResponse prefixResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName)
          .prefix("test-index")
          .maxResults(10));
      assertNotNull(prefixResponse);
      assertNotNull(prefixResponse.indexes());
      assertEquals(2, prefixResponse.indexes().size());

      List<String> prefixIndexNames = prefixResponse.indexes().stream()
          .map(IndexSummary::indexName)
          .toList();
      assertTrue(prefixIndexNames.contains(indexName1));
      assertTrue(prefixIndexNames.contains(indexName2));

      // Test pagination with small maxResults
      ListIndexesResponse paginatedResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName).maxResults(1));
      assertNotNull(paginatedResponse);
      assertNotNull(paginatedResponse.indexes());
      assertEquals(1, paginatedResponse.indexes().size());

      // Test pagination with nextToken if present
      if (paginatedResponse.nextToken() != null) {
        ListIndexesResponse nextPageResponse = vectorsClient.listIndexes(b -> 
            b.vectorBucketName(bucketName)
            .maxResults(1)
            .nextToken(paginatedResponse.nextToken()));
        assertNotNull(nextPageResponse);
        assertNotNull(nextPageResponse.indexes());
        assertTrue(nextPageResponse.indexes().size() <= 1);
      }

      // Validate response structure for each index
      for (IndexSummary index : response.indexes()) {
        assertNotNull(index.indexName());
        assertNotNull(index.indexArn());
        assertNotNull(index.vectorBucketName());
        assertNotNull(index.creationTime());
        assertEquals(bucketName, index.vectorBucketName());
        assertTrue(index.indexName().length() >= 3);
        assertTrue(index.indexName().length() <= 63);
        assertTrue(index.indexArn().startsWith("arn:aws:s3vectors"));
        assertTrue(index.indexArn().contains(bucketName));
        assertTrue(index.indexArn().contains(index.indexName()));
      }

    } finally {
      // Clean up indexes and bucket in finally block regardless of test outcome
      try {
        vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName1));
      } catch (Exception e) {
        System.err.println("Failed to clean up index " + indexName1 + ": " + e.getMessage());
      }
      try {
        vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName2));
      } catch (Exception e) {
        System.err.println("Failed to clean up index " + indexName2 + ": " + e.getMessage());
      }
      try {
        vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName3));
      } catch (Exception e) {
        System.err.println("Failed to clean up index " + indexName3 + ": " + e.getMessage());
      }
      try {
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
      } catch (Exception e) {
        System.err.println("Failed to clean up bucket " + bucketName + ": " + e.getMessage());
      }
    }
  }

  @LocalS3
  @Test
  void testListIndexesEdgeCases(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket-" + UUID.randomUUID();

    try {
      // Create vector bucket
      vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));

      // Test with empty bucket (should return empty list)
      ListIndexesResponse emptyResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName));
      assertNotNull(emptyResponse);
      assertNotNull(emptyResponse.indexes());
      assertEquals(0, emptyResponse.indexes().size());
      assertNull(emptyResponse.nextToken());

      // Test with invalid prefix (should return empty list)
      ListIndexesResponse invalidPrefixResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName)
          .prefix("non-existent-prefix-" + UUID.randomUUID()));
      assertNotNull(invalidPrefixResponse);
      assertNotNull(invalidPrefixResponse.indexes());
      assertEquals(0, invalidPrefixResponse.indexes().size());
      assertNull(invalidPrefixResponse.nextToken());

      // Test with maximum allowed maxResults
      ListIndexesResponse maxResponse = vectorsClient.listIndexes(b -> 
          b.vectorBucketName(bucketName).maxResults(500));
      assertNotNull(maxResponse);
      assertNotNull(maxResponse.indexes());

    } finally {
      // Clean up bucket in finally block
      try {
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
      } catch (Exception e) {
        System.err.println("Failed to clean up bucket " + bucketName + ": " + e.getMessage());
      }
    }
  }
}
