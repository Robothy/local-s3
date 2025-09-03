package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.jupiter.LocalS3;
import io.github.robothy.s3.RealS3;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.DataType;
import software.amazon.awssdk.services.s3vectors.model.DistanceMetric;
import software.amazon.awssdk.services.s3vectors.model.QueryOutputVector;
import software.amazon.awssdk.services.s3vectors.model.QueryVectorsResponse;

public class QueryVectorsIntegrationTest {


  @LocalS3
  @Test
  void testQueryVectors(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket" + UUID.randomUUID();
    String indexName = "test-vector-index" + UUID.randomUUID();

    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));
    int dimension = 5;
    vectorsClient.createIndex(index -> index.vectorBucketName(bucketName).indexName(indexName)
        .dimension(dimension)
        .dataType(DataType.FLOAT32)
        .distanceMetric(DistanceMetric.COSINE)
    );

    try {
      List<Float> vector1Data = List.of(1.1f, 1.2f, 1.0f, 1.2f, 1.3f);
      List<Float> vector2Data = List.of(2.1f, 2.2f, 2.0f, 2.2f, 2.3f);
      List<Float> vector3Data = List.of(3.1f, 3.2f, 3.0f, 3.2f, 3.3f);

      Document vector1Metadata = Document.mapBuilder()
          .putString("name", "Bob")
          .putNumber("age", 18)
          .build();

      vectorsClient.putVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
          .vectors(
              v -> v.key("key1")
                  .data(d -> d.float32(vector1Data))
                  .metadata(vector1Metadata)
                  .build(),

              v -> v.key("key2")
                  .data(d -> d.float32(vector2Data)).build(),

              v -> v.key("key3")
                  .data(d -> d.float32(vector3Data)).build()
          ));

      List<Float> queryVector = List.of(1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
      QueryVectorsResponse queryVectorsResponse =
          vectorsClient.queryVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
              .queryVector(q -> q.float32(queryVector))
              .topK(3)
              .returnMetadata(true)
              .returnDistance(true)
          );

      queryVectorsResponse.vectors().forEach(vector -> {
        System.out.println(vector.key() + " - distance - " + vector.distance());
      });

      assertEquals(3, queryVectorsResponse.vectors().size());
      QueryOutputVector queryOutputVector1 = queryVectorsResponse.vectors().get(0);
      assertEquals("key3", queryOutputVector1.key());
      assertNotNull(queryOutputVector1.distance());
      assertEquals(7.5495243E-4f, queryOutputVector1.distance(), 0.1f);

      QueryOutputVector queryOutputVector2 = queryVectorsResponse.vectors().get(1);
      assertEquals("key2", queryOutputVector2.key());
      assertNotNull(queryOutputVector2.distance());
      assertEquals(0.0016186237f, queryOutputVector2.distance(), 0.1f);

      QueryOutputVector queryOutputVector3 = queryVectorsResponse.vectors().get(2);
      assertEquals("key1", queryOutputVector3.key());
      assertNotNull(queryOutputVector3.distance());
      assertEquals(0.003602028f, queryOutputVector3.distance(), 0.1f);
      assertEquals(vector1Metadata, queryOutputVector3.metadata());
    } finally {
      vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
      vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
    }
  }

  @LocalS3
  @Test
  void testQueryVectorsWithEuclideanDistance(S3VectorsClient vectorsClient) {
    String bucketName = "test-euclidean-bucket" + UUID.randomUUID();
    String indexName = "test-euclidean-index" + UUID.randomUUID();

    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));
    int dimension = 5;
    vectorsClient.createIndex(index -> index.vectorBucketName(bucketName).indexName(indexName)
        .dimension(dimension)
        .dataType(DataType.FLOAT32)
        .distanceMetric(DistanceMetric.EUCLIDEAN)
    );

    try {
      List<Float> vector1Data = List.of(1.1f, 1.2f, 1.0f, 1.2f, 1.3f);
      List<Float> vector2Data = List.of(2.1f, 2.2f, 2.0f, 2.2f, 2.3f);
      List<Float> vector3Data = List.of(3.1f, 3.2f, 3.0f, 3.2f, 3.3f);

      Document vector1Metadata = Document.mapBuilder()
          .putString("name", "Alice")
          .putNumber("score", 95)
          .build();

      vectorsClient.putVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
          .vectors(
              v -> v.key("euclidean1")
                  .data(d -> d.float32(vector1Data))
                  .metadata(vector1Metadata)
                  .build(),

              v -> v.key("euclidean2")
                  .data(d -> d.float32(vector2Data)).build(),

              v -> v.key("euclidean3")
                  .data(d -> d.float32(vector3Data)).build()
          ));

      List<Float> queryVector = List.of(1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
      QueryVectorsResponse queryVectorsResponse =
          vectorsClient.queryVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
              .queryVector(q -> q.float32(queryVector))
              .topK(3)
              .returnMetadata(true)
              .returnDistance(true)
          );

      queryVectorsResponse.vectors().forEach(vector -> {
        System.out.println(vector.key() + " - euclidean distance - " + vector.distance());
      });

      assertEquals(3, queryVectorsResponse.vectors().size());
      // With Euclidean distance, vector1 (euclidean1) should be closest to the query vector [1,1,1,1,1]
      QueryOutputVector queryOutputVector1 = queryVectorsResponse.vectors().get(0);
      assertEquals("euclidean1", queryOutputVector1.key());
      assertNotNull(queryOutputVector1.distance());
      assertEquals(0.4242641f, queryOutputVector1.distance(), 0.01f);
      assertEquals(vector1Metadata, queryOutputVector1.metadata());

      QueryOutputVector queryOutputVector2 = queryVectorsResponse.vectors().get(1);
      assertEquals("euclidean2", queryOutputVector2.key());
      assertNotNull(queryOutputVector2.distance());
      assertEquals(2.6038435f, queryOutputVector2.distance(), 0.01f);

      QueryOutputVector queryOutputVector3 = queryVectorsResponse.vectors().get(2);
      assertEquals("euclidean3", queryOutputVector3.key());
      assertNotNull(queryOutputVector3.distance());
      assertEquals(4.835165f, queryOutputVector3.distance(), 0.01f);
    } finally {
      vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
      vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
    }
  }

  @LocalS3
  @Test
  void testQueryVectorsWithMetadataFilter(S3VectorsClient vectorsClient) {
    String bucketName = "test-filter-bucket" + UUID.randomUUID();
    String indexName = "test-filter-index" + UUID.randomUUID();

    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));
    int dimension = 3;
    vectorsClient.createIndex(index -> index.vectorBucketName(bucketName).indexName(indexName)
        .dimension(dimension)
        .dataType(DataType.FLOAT32)
        .distanceMetric(DistanceMetric.EUCLIDEAN)
    );

    try {
      // Create test vectors with different metadata
      List<Float> vector1Data = List.of(1.0f, 1.0f, 1.0f);
      List<Float> vector2Data = List.of(2.0f, 2.0f, 2.0f);
      List<Float> vector3Data = List.of(3.0f, 3.0f, 3.0f);
      List<Float> vector4Data = List.of(4.0f, 4.0f, 4.0f);

      Document vector1Metadata = Document.mapBuilder()
          .putString("genre", "documentary")
          .putNumber("year", 2020)
          .putNumber("rating", 8.5)
          .putBoolean("available", true)
          .putList("categories", listBuilder -> listBuilder
              .addString("film")
              .addString("educational"))
          .build();

      Document vector2Metadata = Document.mapBuilder()
          .putString("genre", "drama")
          .putNumber("year", 2019)
          .putNumber("rating", 7.8)
          .putBoolean("available", false)
          .build();

      Document vector3Metadata = Document.mapBuilder()
          .putString("genre", "comedy")
          .putNumber("year", 2021)
          .putNumber("rating", 6.2)
          .putBoolean("available", true)
          .putList("categories", listBuilder -> listBuilder
              .addString("documentary")
              .addString("humor"))
          .build();

      // Vector 4 has no metadata
      vectorsClient.putVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
          .vectors(
              v -> v.key("vector1")
                  .data(d -> d.float32(vector1Data))
                  .metadata(vector1Metadata),

              v -> v.key("vector2")
                  .data(d -> d.float32(vector2Data))
                  .metadata(vector2Metadata),

              v -> v.key("vector3")
                  .data(d -> d.float32(vector3Data))
                  .metadata(vector3Metadata),

              v -> v.key("vector4")
                  .data(d -> d.float32(vector4Data))
              // No metadata
          ));

      List<Float> queryVector = List.of(0.0f, 0.0f, 0.0f);

      // Test 1: Simple equality filter (implicit $eq)
      Document simpleEqFilter = Document.mapBuilder()
          .putString("genre", "documentary")
          .build();
      QueryVectorsResponse response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(simpleEqFilter)
      );

      assertEquals(1, response.vectors().size(), "Simple equality filter should return 1 vector");
      assertEquals("vector1", response.vectors().get(0).key());

      // Test 2: Numeric range filter (year > 2019)
      Document numericFilter = Document.mapBuilder()
          .putDocument("year", Document.mapBuilder()
              .putNumber("$gt", 2019)
              .build())
          .build();

      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(numericFilter)
      );

      assertEquals(2, response.vectors().size(), "Numeric filter should return 2 vectors (2020 and 2021)");
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector1")));
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector3")));

      // Test 3: Array membership filter ($in operator)
      Document inFilter = Document.mapBuilder()
          .putDocument("genre", Document.mapBuilder()
              .putList("$in", listBuilder -> listBuilder
                  .addString("comedy")
                  .addString("documentary"))
              .build())
          .build();

      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(inFilter)
      );

      assertEquals(2, response.vectors().size(), "$in filter should return 2 vectors");
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector1")));
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector3")));

      // Test 4: Logical AND filter
      Document andFilter = Document.mapBuilder()
          .putList("$and", listBuilder -> listBuilder
              .addDocument(Document.mapBuilder()
                  .putBoolean("available", true)
                  .build())
              .addDocument(Document.mapBuilder()
                  .putDocument("year", Document.mapBuilder()
                      .putNumber("$gte", 2020)
                      .build())
                  .build()))
          .build();

      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(andFilter)
      );

      assertEquals(2, response.vectors().size(), "$and filter should return 2 vectors");
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector1")));
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector3")));

      // Test 5: Array field equality (should match vector3 which has "documentary" in categories)
      Document arrayFilter = Document.mapBuilder()
          .putString("categories", "documentary")
          .build();

      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(arrayFilter)
      );

      assertEquals(1, response.vectors().size(), "Array field filter should return 1 vector");
      assertEquals("vector3", response.vectors().get(0).key());

      // Test 6: $exists filter
      Document existsFilter = Document.mapBuilder()
          .putDocument("categories", Document.mapBuilder()
              .putBoolean("$exists", true)
              .build())
          .build();

      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
          .filter(existsFilter)
      );

      assertEquals(2, response.vectors().size(), "$exists filter should return 2 vectors with categories");
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector1")));
      assertTrue(response.vectors().stream().anyMatch(v -> v.key().equals("vector3")));

      // Test 7: No filter should return all vectors
      response = vectorsClient.queryVectors(b -> b
          .vectorBucketName(bucketName)
          .indexName(indexName)
          .queryVector(q -> q.float32(queryVector))
          .topK(10)
          .returnMetadata(true)
      );

      assertEquals(4, response.vectors().size(), "No filter should return all 4 vectors");

    } finally {
      vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
      vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
    }

  }

}
