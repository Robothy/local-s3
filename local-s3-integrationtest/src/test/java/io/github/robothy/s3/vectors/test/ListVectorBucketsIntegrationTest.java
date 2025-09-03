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
import software.amazon.awssdk.services.s3vectors.model.ListVectorBucketsResponse;
import software.amazon.awssdk.services.s3vectors.model.VectorBucketSummary;

public class ListVectorBucketsIntegrationTest {

  @LocalS3
  @Test
  void testListVectorBuckets(S3VectorsClient vectorsClient) {
    String bucketName1 = "test-vector-bucket-" + UUID.randomUUID();
    String bucketName2 = "test-vector-bucket-" + UUID.randomUUID();
    String bucketName3 = "different-prefix-" + UUID.randomUUID();

    try {
      // Test listing when no buckets exist
      ListVectorBucketsResponse emptyResponse = vectorsClient.listVectorBuckets(b -> b.maxResults(10));
      assertNotNull(emptyResponse);
      assertNotNull(emptyResponse.vectorBuckets());

      // Create test buckets
      vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName1));
      vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName2));
      vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName3));

      // Test basic listing without filters
      ListVectorBucketsResponse response = vectorsClient.listVectorBuckets(b -> b.maxResults(10));
      assertNotNull(response);
      assertNotNull(response.vectorBuckets());
      assertTrue(response.vectorBuckets().size() >= 3);

      // Verify all created buckets are present
      List<String> bucketNames = response.vectorBuckets().stream()
          .map(VectorBucketSummary::vectorBucketName)
          .toList();
      assertTrue(bucketNames.contains(bucketName1));
      assertTrue(bucketNames.contains(bucketName2));
      assertTrue(bucketNames.contains(bucketName3));

      // Test prefix filtering
      ListVectorBucketsResponse prefixResponse = vectorsClient.listVectorBuckets(b -> 
          b.prefix("test-vector-bucket").maxResults(10));
      assertNotNull(prefixResponse);
      assertNotNull(prefixResponse.vectorBuckets());
      assertTrue(prefixResponse.vectorBuckets().size() >= 2);

      List<String> prefixBucketNames = prefixResponse.vectorBuckets().stream()
          .map(VectorBucketSummary::vectorBucketName)
          .toList();
      assertTrue(prefixBucketNames.contains(bucketName1));
      assertTrue(prefixBucketNames.contains(bucketName2));

      // Test pagination with small maxResults
      ListVectorBucketsResponse paginatedResponse = vectorsClient.listVectorBuckets(b -> b.maxResults(1));
      assertNotNull(paginatedResponse);
      assertNotNull(paginatedResponse.vectorBuckets());
      assertTrue(paginatedResponse.vectorBuckets().size() <= 1);

      // Test pagination with nextToken if present
      if (paginatedResponse.nextToken() != null) {
        ListVectorBucketsResponse nextPageResponse = vectorsClient.listVectorBuckets(b -> 
            b.maxResults(1).nextToken(paginatedResponse.nextToken()));
        assertNotNull(nextPageResponse);
        assertNotNull(nextPageResponse.vectorBuckets());
      }

      // Validate response structure for each bucket
      for (VectorBucketSummary bucket : response.vectorBuckets()) {
        assertNotNull(bucket.vectorBucketName());
        assertNotNull(bucket.vectorBucketArn());
        assertNotNull(bucket.creationTime());
        assertTrue(bucket.vectorBucketName().length() >= 3);
        assertTrue(bucket.vectorBucketName().length() <= 63);
        assertTrue(bucket.vectorBucketArn().startsWith("arn:aws:s3vectors"));
      }

    } finally {
      // Clean up test buckets in finally block regardless of test outcome
      try {
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName1));
      } catch (Exception e) {
        // Log but don't fail the test if cleanup fails
        System.err.println("Failed to clean up bucket " + bucketName1 + ": " + e.getMessage());
      }
      try {
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName2));
      } catch (Exception e) {
        System.err.println("Failed to clean up bucket " + bucketName2 + ": " + e.getMessage());
      }
      try {
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName3));
      } catch (Exception e) {
        System.err.println("Failed to clean up bucket " + bucketName3 + ": " + e.getMessage());
      }
    }
  }

  @LocalS3
  @Test
  void testListVectorBucketsEdgeCases(S3VectorsClient vectorsClient) {
    // Test with empty request (all defaults)
    ListVectorBucketsResponse defaultResponse = vectorsClient.listVectorBuckets(b -> {});
    assertNotNull(defaultResponse);
    assertNotNull(defaultResponse.vectorBuckets());

    // Test with invalid prefix (should return empty list)
    ListVectorBucketsResponse invalidPrefixResponse = vectorsClient.listVectorBuckets(b -> 
        b.prefix("non-existent-prefix-" + UUID.randomUUID()));
    assertNotNull(invalidPrefixResponse);
    assertNotNull(invalidPrefixResponse.vectorBuckets());
    assertEquals(0, invalidPrefixResponse.vectorBuckets().size());
    assertNull(invalidPrefixResponse.nextToken());

    // Test with maximum allowed maxResults
    ListVectorBucketsResponse maxResponse = vectorsClient.listVectorBuckets(b -> b.maxResults(500));
    assertNotNull(maxResponse);
    assertNotNull(maxResponse.vectorBuckets());
  }
}
