package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.jupiter.LocalS3;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.CreateVectorBucketResponse;
import software.amazon.awssdk.services.s3vectors.model.DeleteVectorBucketResponse;
import software.amazon.awssdk.services.s3vectors.model.GetVectorBucketResponse;
import software.amazon.awssdk.services.s3vectors.model.NotFoundException;
import software.amazon.awssdk.services.s3vectors.model.VectorBucket;

public class VectorBucketIntegrationTest {

  @LocalS3
  @Test
  void testCreateGetListDeleteVectorBucket(S3VectorsClient vectorsClient) {
    String vectorBucketName1 = "test-vector-bucket" + UUID.randomUUID();
    CreateVectorBucketResponse createVectorBucketResponse = vectorsClient.createVectorBucket(b -> b.vectorBucketName(vectorBucketName1));
    assertNotNull(createVectorBucketResponse);
    GetVectorBucketResponse getVectorBucketResponse = vectorsClient.getVectorBucket(b -> b.vectorBucketName(vectorBucketName1));
    VectorBucket gotVectorBucket = getVectorBucketResponse.vectorBucket();
    assertTrue(gotVectorBucket.creationTime().isAfter(Instant.now().minus(Duration.ofMinutes(10))));
    assertTrue(gotVectorBucket.creationTime().isBefore(Instant.now().plus(Duration.ofMinutes(10))));
    assertEquals(vectorBucketName1, gotVectorBucket.vectorBucketName());

    DeleteVectorBucketResponse deleteVectorBucketResponse =
        vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(vectorBucketName1));
    assertNotNull(deleteVectorBucketResponse);
    assertThrows(NotFoundException.class, () -> vectorsClient.getVectorBucket(b -> b.vectorBucketName(vectorBucketName1)));
  }

}
