package io.github.robothy.s3.vectors.test;

import com.robothy.s3.jupiter.LocalS3;
import io.github.robothy.s3.RealS3;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.document.internal.MapDocument;
import software.amazon.awssdk.core.document.internal.StringDocument;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ListVectors operation against LocalS3.
 * This test validates the implementation against the LocalS3 mock service.
 */
public class ListVectorsIntegrationTest {

  // Test data constants
  private static final int DEFAULT_DIMENSION = 3;
  private static final DistanceMetric DEFAULT_DISTANCE_METRIC = DistanceMetric.COSINE;
  private static final DataType DEFAULT_DATA_TYPE = DataType.FLOAT32;

  /**
   * Test environment setup helper
   */
  private static class TestEnvironment {
    final String bucketName;
    final String indexName;
    final S3VectorsClient client;
    String indexArn;

    TestEnvironment(S3VectorsClient client, String testName) {
      this.client = client;
      this.bucketName = "test-" + testName + "-bucket-" + System.currentTimeMillis();
      this.indexName = "test-" + testName + "-index-" + System.currentTimeMillis();
    }

    void createVectorBucket() {
      CreateVectorBucketRequest request = CreateVectorBucketRequest.builder()
          .vectorBucketName(bucketName)
          .build();
      client.createVectorBucket(request);
    }

    void createIndex() {
      createIndex(DEFAULT_DIMENSION, DEFAULT_DISTANCE_METRIC, DEFAULT_DATA_TYPE);
    }

    void createIndex(int dimension, DistanceMetric distanceMetric, DataType dataType) {
      CreateIndexRequest request = CreateIndexRequest.builder()
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .dimension(dimension)
          .distanceMetric(distanceMetric)
          .dataType(dataType)
          .build();
      client.createIndex(request);
      
      // Get ARN for later use
      GetIndexResponse getIndexResponse = client.getIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
      this.indexArn = getIndexResponse.index().indexArn();
    }

    void putVectors(List<PutInputVector> vectors) {
      PutVectorsRequest request = PutVectorsRequest.builder()
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .vectors(vectors)
          .build();
      client.putVectors(request);
    }

    void cleanup(List<String> vectorKeys) {
      // Delete vectors
      if (vectorKeys != null && !vectorKeys.isEmpty()) {
        try {
          DeleteVectorsRequest deleteVectorsRequest = DeleteVectorsRequest.builder()
              .vectorBucketName(bucketName)
              .indexName(indexName)
              .keys(vectorKeys)
              .build();
          client.deleteVectors(deleteVectorsRequest);
        } catch (Exception e) {
          System.err.println("Failed to delete vectors during cleanup: " + e.getMessage());
        }
      }

      // Delete index
      try {
        DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.builder()
            .vectorBucketName(bucketName)
            .indexName(indexName)
            .build();
        client.deleteIndex(deleteIndexRequest);
      } catch (Exception e) {
        System.err.println("Failed to delete index during cleanup: " + e.getMessage());
      }

      // Delete bucket
      try {
        DeleteVectorBucketRequest deleteBucketRequest = DeleteVectorBucketRequest.builder()
            .vectorBucketName(bucketName)
            .build();
        client.deleteVectorBucket(deleteBucketRequest);
      } catch (Exception e) {
        System.err.println("Failed to delete vector bucket during cleanup: " + e.getMessage());
      }
    }
  }

  /**
   * Helper methods for creating test vectors
   */
  private static List<PutInputVector> createTestVectors(String prefix, int count) {
    return IntStream.range(1, count + 1)
        .mapToObj(i -> PutInputVector.builder()
            .key(prefix + String.format("%02d", i))
            .data(VectorData.builder()
                .float32(List.of((float) i, (float) (i + 1), (float) (i + 2)))
                .build())
            .metadata(new MapDocument(Map.of(
                "category", new StringDocument(i % 2 == 0 ? "prod" : "test"),
                "priority", new StringDocument(i % 3 == 0 ? "high" : i % 3 == 1 ? "medium" : "low")
            )))
            .build())
        .toList();
  }

  private static List<PutInputVector> createSimpleTestVectors(String prefix, int count, int dimension) {
    return IntStream.range(1, count + 1)
        .mapToObj(i -> {
          List<Float> data = IntStream.range(0, dimension)
              .mapToObj(j -> (float) (i * dimension + j))
              .toList();
          return PutInputVector.builder()
              .key(prefix + String.format("%02d", i))
              .data(VectorData.builder().float32(data).build())
              .build();
        })
        .toList();
  }

  private static List<String> extractKeys(String prefix, int count) {
    return IntStream.range(1, count + 1)
        .mapToObj(i -> prefix + String.format("%02d", i))
        .toList();
  }

  /**
   * Helper methods for assertions
   */
  private static void assertVectorKeysPresent(ListVectorsResponse response, String... expectedKeys) {
    List<String> actualKeys = response.vectors().stream()
        .map(ListOutputVector::key)
        .toList();
    for (String expectedKey : expectedKeys) {
      assertTrue(actualKeys.contains(expectedKey), "Expected key " + expectedKey + " not found");
    }
  }

  private static void assertNoDataOrMetadata(ListVectorsResponse response) {
    for (ListOutputVector vector : response.vectors()) {
      assertNull(vector.data(), "Data should be null when not requested");
      assertNull(vector.metadata(), "Metadata should be null when not requested");
    }
  }

  private static void assertDataPresent(ListVectorsResponse response) {
    for (ListOutputVector vector : response.vectors()) {
      assertNotNull(vector.data(), "Data should be present when requested");
      assertNotNull(vector.data().float32(), "Float32 data should be present");
      assertTrue(vector.data().float32().size() > 0, "Data should not be empty");
    }
  }

  private static void assertMetadataPresent(ListVectorsResponse response) {
    for (ListOutputVector vector : response.vectors()) {
      assertNotNull(vector.metadata(), "Metadata should be present when requested");
    }
  }

  private static void assertNoOverlapBetweenSegments(List<ListVectorsResponse> segmentResponses) {
    List<String> allKeys = new ArrayList<>();
    for (ListVectorsResponse response : segmentResponses) {
      List<String> segmentKeys = response.vectors().stream()
          .map(ListOutputVector::key)
          .toList();
      
      for (String key : segmentKeys) {
        assertFalse(allKeys.contains(key), "Vector " + key + " appears in multiple segments");
        allKeys.add(key);
      }
    }
  }

  @LocalS3
  @Test
  void testBasicListVectors(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "basic-list");
    List<PutInputVector> testVectors = createTestVectors("vector", 3);
    List<String> vectorKeys = extractKeys("vector", 3);

    try {
      env.createVectorBucket();
      env.createIndex();
      env.putVectors(testVectors);

      // Test basic listing without metadata or data
      ListVectorsRequest request = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .build();
      ListVectorsResponse response = vectorsClient.listVectors(request);

      assertNotNull(response);
      assertNotNull(response.vectors());
      assertEquals(3, response.vectors().size());
      assertVectorKeysPresent(response, "vector01", "vector02", "vector03");
      assertNoDataOrMetadata(response);

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsWithDataAndMetadata(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "data-metadata");
    List<PutInputVector> testVectors = createTestVectors("vector", 3);
    List<String> vectorKeys = extractKeys("vector", 3);

    try {
      env.createVectorBucket();
      env.createIndex();
      env.putVectors(testVectors);

      // Test listing with metadata only
      ListVectorsRequest metadataRequest = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .returnMetadata(true)
          .build();
      ListVectorsResponse metadataResponse = vectorsClient.listVectors(metadataRequest);

      assertEquals(3, metadataResponse.vectors().size());
      assertMetadataPresent(metadataResponse);
      for (ListOutputVector vector : metadataResponse.vectors()) {
        assertNull(vector.data(), "Data should be null when not requested");
      }

      // Test listing with data only
      ListVectorsRequest dataRequest = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .returnData(true)
          .build();
      ListVectorsResponse dataResponse = vectorsClient.listVectors(dataRequest);

      assertEquals(3, dataResponse.vectors().size());
      assertDataPresent(dataResponse);
      for (ListOutputVector vector : dataResponse.vectors()) {
        assertNull(vector.metadata(), "Metadata should be null when not requested");
        assertEquals(3, vector.data().float32().size());
      }

      // Test listing with both data and metadata
      ListVectorsRequest bothRequest = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .returnData(true)
          .returnMetadata(true)
          .build();
      ListVectorsResponse bothResponse = vectorsClient.listVectors(bothRequest);

      assertEquals(3, bothResponse.vectors().size());
      assertDataPresent(bothResponse);
      assertMetadataPresent(bothResponse);

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsPagination(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "pagination");
    List<PutInputVector> testVectors = createTestVectors("vector", 3);
    List<String> vectorKeys = extractKeys("vector", 3);

    try {
      env.createVectorBucket();
      env.createIndex();
      env.putVectors(testVectors);

      // Test pagination with maxResults
      ListVectorsRequest firstPageRequest = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .maxResults(2)
          .build();
      ListVectorsResponse firstPageResponse = vectorsClient.listVectors(firstPageRequest);

      assertEquals(2, firstPageResponse.vectors().size());
      assertNotNull(firstPageResponse.nextToken());

      // Test next page using pagination token
      ListVectorsRequest secondPageRequest = ListVectorsRequest.builder()
          .vectorBucketName(env.bucketName)
          .indexName(env.indexName)
          .maxResults(2)
          .nextToken(firstPageResponse.nextToken())
          .build();
      ListVectorsResponse secondPageResponse = vectorsClient.listVectors(secondPageRequest);

      assertEquals(1, secondPageResponse.vectors().size()); // Should have 1 remaining vector
      assertNull(secondPageResponse.nextToken()); // Should be null as this is the last page

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsWithIndexArn(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "arn");
    List<PutInputVector> testVectors = createSimpleTestVectors("arn-vector", 1, 2);
    List<String> vectorKeys = List.of("arn-vector01");

    try {
      env.createVectorBucket();
      env.createIndex(2, DistanceMetric.EUCLIDEAN, DataType.FLOAT32);
      env.putVectors(testVectors);

      // Test listing using index ARN instead of bucket name + index name
      ListVectorsRequest request = ListVectorsRequest.builder()
          .indexArn(env.indexArn)
          .build();
      ListVectorsResponse response = vectorsClient.listVectors(request);

      assertNotNull(response);
      assertNotNull(response.vectors());
      assertEquals(1, response.vectors().size());
      assertEquals("arn-vector01", response.vectors().get(0).key());

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsWithSegments(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "segments");
    List<PutInputVector> testVectors = createSimpleTestVectors("vector-segment", 6, DEFAULT_DIMENSION);
    List<String> vectorKeys = extractKeys("vector-segment", 6);

    try {
      env.createVectorBucket();
      env.createIndex();
      env.putVectors(testVectors);

      // Test single segment (sequential operation)
      ListVectorsResponse singleSegmentResponse = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(1)
              .segmentIndex(0)
              .build());

      assertEquals(6, singleSegmentResponse.vectors().size());

      // Test multiple segments - divide into 2 segments
      ListVectorsResponse segment0Response = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(2)
              .segmentIndex(0)
              .build());

      ListVectorsResponse segment1Response = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(2)
              .segmentIndex(1)
              .build());

      // Combined segments should contain all vectors
      int totalVectorsFromSegments = segment0Response.vectors().size() + segment1Response.vectors().size();
      assertEquals(6, totalVectorsFromSegments);

      // Verify no overlap between segments
      assertNoOverlapBetweenSegments(List.of(segment0Response, segment1Response));

      // Test 3 segments
      List<ListVectorsResponse> threeSegmentResponses = IntStream.range(0, 3)
          .mapToObj(i -> vectorsClient.listVectors(
              ListVectorsRequest.builder()
                  .vectorBucketName(env.bucketName)
                  .indexName(env.indexName)
                  .segmentCount(3)
                  .segmentIndex(i)
                  .build()))
          .toList();

      int totalFrom3Segments = threeSegmentResponses.stream()
          .mapToInt(response -> response.vectors().size())
          .sum();
      assertEquals(6, totalFrom3Segments);
      assertNoOverlapBetweenSegments(threeSegmentResponses);

      // Test segments with returnData
      ListVectorsResponse segmentWithDataResponse = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(2)
              .segmentIndex(0)
              .returnData(true)
              .build());

      assertTrue(segmentWithDataResponse.vectors().size() > 0);
      assertDataPresent(segmentWithDataResponse);
      for (ListOutputVector vector : segmentWithDataResponse.vectors()) {
        assertNull(vector.metadata());
      }

      // Test segments with maxResults
      ListVectorsResponse segmentWithMaxResultsResponse = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(2)
              .segmentIndex(0)
              .maxResults(2)
              .build());

      assertTrue(segmentWithMaxResultsResponse.vectors().size() <= 2);

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsSegmentationEdgeCases(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "edge-segments");
    List<PutInputVector> testVectors = createSimpleTestVectors("single-vector", 1, 2);
    List<String> vectorKeys = List.of("single-vector01");

    try {
      env.createVectorBucket();
      env.createIndex(2, DistanceMetric.EUCLIDEAN, DataType.FLOAT32);
      env.putVectors(testVectors);

      // Test edge case: More segments than vectors
      List<ListVectorsResponse> manySegmentResponses = IntStream.range(0, 5)
          .mapToObj(i -> vectorsClient.listVectors(
              ListVectorsRequest.builder()
                  .vectorBucketName(env.bucketName)
                  .indexName(env.indexName)
                  .segmentCount(5)
                  .segmentIndex(i)
                  .build()))
          .toList();

      // At least one segment should contain the vector
      int totalVectors = manySegmentResponses.stream()
          .mapToInt(response -> response.vectors().size())
          .sum();
      assertTrue(totalVectors >= 1, "At least one segment should contain the vector");

      // Test maximum segment count (16)
      ListVectorsResponse maxSegmentResponse = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .vectorBucketName(env.bucketName)
              .indexName(env.indexName)
              .segmentCount(16)
              .segmentIndex(15)  // Last valid segment index
              .build());

      assertNotNull(maxSegmentResponse);
      assertNotNull(maxSegmentResponse.vectors());

      // Test with empty index
      TestEnvironment emptyEnv = new TestEnvironment(vectorsClient, "empty-index");
      try {
        emptyEnv.createVectorBucket();
        emptyEnv.createIndex(2, DistanceMetric.EUCLIDEAN, DataType.FLOAT32);

        ListVectorsResponse emptyIndexResponse = vectorsClient.listVectors(
            ListVectorsRequest.builder()
                .vectorBucketName(emptyEnv.bucketName)
                .indexName(emptyEnv.indexName)
                .segmentCount(3)
                .segmentIndex(0)
                .build());

        assertNotNull(emptyIndexResponse);
        assertNotNull(emptyIndexResponse.vectors());
        assertEquals(0, emptyIndexResponse.vectors().size());

      } finally {
        emptyEnv.cleanup(List.of());
      }

    } finally {
      env.cleanup(vectorKeys);
    }
  }

  @LocalS3
  @Test
  void testListVectorsSegmentationWithIndexArn(S3VectorsClient vectorsClient) {
    TestEnvironment env = new TestEnvironment(vectorsClient, "arn-segments");
    List<PutInputVector> testVectors = createSimpleTestVectors("arn-vector", 4, DEFAULT_DIMENSION);
    List<String> vectorKeys = extractKeys("arn-vector", 4);

    try {
      env.createVectorBucket();
      env.createIndex();
      env.putVectors(testVectors);

      // Test segmentation using index ARN
      ListVectorsResponse segment0Response = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .indexArn(env.indexArn)
              .segmentCount(2)
              .segmentIndex(0)
              .build());

      ListVectorsResponse segment1Response = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .indexArn(env.indexArn)
              .segmentCount(2)
              .segmentIndex(1)
              .build());

      // Combined segments should contain all vectors
      int totalVectorsFromSegments = segment0Response.vectors().size() + segment1Response.vectors().size();
      assertEquals(4, totalVectorsFromSegments);

      // Verify no overlap between segments
      assertNoOverlapBetweenSegments(List.of(segment0Response, segment1Response));

      // Test combining segmentation with other parameters using ARN
      ListVectorsResponse segmentWithOptionsResponse = vectorsClient.listVectors(
          ListVectorsRequest.builder()
              .indexArn(env.indexArn)
              .segmentCount(2)
              .segmentIndex(0)
              .returnData(true)
              .maxResults(2)
              .build());

      assertTrue(segmentWithOptionsResponse.vectors().size() <= 2);
      if (!segmentWithOptionsResponse.vectors().isEmpty()) {
        assertDataPresent(segmentWithOptionsResponse);
        for (ListOutputVector vector : segmentWithOptionsResponse.vectors()) {
          assertNull(vector.metadata());
        }
      }

    } finally {
      env.cleanup(vectorKeys);
    }
  }

}
