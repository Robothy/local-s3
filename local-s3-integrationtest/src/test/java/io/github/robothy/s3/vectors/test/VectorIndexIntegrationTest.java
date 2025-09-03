package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.jupiter.LocalS3;
import io.github.robothy.s3.RealS3;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.CreateIndexResponse;
import software.amazon.awssdk.services.s3vectors.model.DataType;
import software.amazon.awssdk.services.s3vectors.model.DeleteIndexResponse;
import software.amazon.awssdk.services.s3vectors.model.DistanceMetric;
import software.amazon.awssdk.services.s3vectors.model.GetIndexResponse;
import software.amazon.awssdk.services.s3vectors.model.Index;
import software.amazon.awssdk.services.s3vectors.model.NotFoundException;
import software.amazon.awssdk.services.s3vectors.model.S3VectorsResponseMetadata;

public class VectorIndexIntegrationTest {

  @LocalS3
  @Test
  void testCreateGetDeleteVectorIndex(S3VectorsClient vectorsClient) {
    String bucketName = setupBucket(vectorsClient);

    String indexName = "test-vector-index-" + UUID.randomUUID();
    CreateIndexResponse createIndexResponse = vectorsClient.createIndex(b -> b.vectorBucketName(bucketName)
        .indexName(indexName)
        .dataType(DataType.FLOAT32)
        .dimension(10)
        .distanceMetric(DistanceMetric.EUCLIDEAN)
        .metadataConfiguration(m -> m.nonFilterableMetadataKeys("nf1", "nf2")));

    GetIndexResponse getIndexResponse = vectorsClient.getIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
    Index index = getIndexResponse.index();
    assertEquals(indexName, index.indexName());
    assertTrue(index.creationTime().isAfter(Instant.now().minus(Duration.ofMinutes(10))));
    assertTrue(index.creationTime().isBefore(Instant.now().plus(Duration.ofMinutes(10))));
    assertEquals(DataType.FLOAT32, index.dataType());
    assertEquals(10, index.dimension());
    assertEquals(DistanceMetric.EUCLIDEAN, index.distanceMetric());
    assertEquals(2, index.metadataConfiguration().nonFilterableMetadataKeys().size());
    assertTrue(index.metadataConfiguration().nonFilterableMetadataKeys().contains("nf1"));
    assertTrue(index.metadataConfiguration().nonFilterableMetadataKeys().contains("nf2"));
    S3VectorsResponseMetadata metadata = createIndexResponse.responseMetadata();
    assertTrue(metadata.requestId() != null && !metadata.requestId().isEmpty());


    DeleteIndexResponse deleteIndexResponse = vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
    assertTrue(deleteIndexResponse.responseMetadata().requestId() != null && !deleteIndexResponse.responseMetadata().requestId().isEmpty());

    assertThrows(NotFoundException.class, () -> vectorsClient.getIndex(b -> b.vectorBucketName(bucketName).indexName(indexName)));

    cleanupBucket(vectorsClient, bucketName);
  }

  String setupBucket(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket-" + UUID.randomUUID();
    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));
    return bucketName;
  }

  void cleanupBucket(S3VectorsClient vectorsClient, String bucketName) {
    vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
  }

}
