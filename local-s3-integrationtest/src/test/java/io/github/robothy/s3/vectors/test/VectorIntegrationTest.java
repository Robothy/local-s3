package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.robothy.s3.jupiter.LocalS3;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkNumber;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.core.document.internal.MapDocument;
import software.amazon.awssdk.core.document.internal.NumberDocument;
import software.amazon.awssdk.core.document.internal.StringDocument;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.DataType;
import software.amazon.awssdk.services.s3vectors.model.DistanceMetric;
import software.amazon.awssdk.services.s3vectors.model.GetOutputVector;
import software.amazon.awssdk.services.s3vectors.model.GetVectorsResponse;
import software.amazon.awssdk.services.s3vectors.model.PutVectorsResponse;

public class VectorIntegrationTest {

  @LocalS3
  @Test
  void testCreateGetDeleteVectorIndex(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket" + UUID.randomUUID();
    String indexName = "test-vector-index" + UUID.randomUUID();
    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));

    int dimension = 10;
    vectorsClient.createIndex(index -> index.vectorBucketName(bucketName).indexName(indexName)
        .dimension(dimension)
        .dataType(DataType.FLOAT32)
        .distanceMetric(DistanceMetric.COSINE)
    );

    try {
      List<Float> vector1Data = List.of(1.1f, 1.2f, 1.0f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f);
      List<Float> vector2Data = List.of(2.1f, 2.2f, 2.0f, 2.2f, 2.3f, 2.4f, 2.5f, 2.6f, 2.7f, 2.8f);
      List<Float> vector3Data = List.of(3.1f, 3.2f, 3.0f, 3.2f, 3.3f, 3.4f, 3.5f, 3.6f, 3.7f, 3.8f);

      PutVectorsResponse putVectorsResponse = vectorsClient.putVectors(b -> b.vectorBucketName(bucketName).indexName(indexName)
          .vectors(
              v -> v.key("key1")
                  .data(d -> d.float32(vector1Data)).build(),

              v -> v.key("key2")
                  .data(d -> d.float32(vector2Data)).build(),

              v -> v.key("key3")
                  .metadata(new MapDocument(new HashMap<String, Document>() {{
                    put("meta1", new NumberDocument(SdkNumber.fromInteger(6)));
                    put("meta2", new StringDocument("value"));
                  }}))
                  .data(d -> d.float32(vector3Data)).build()
          ));
      assertNotNull(putVectorsResponse.responseMetadata().requestId());

      GetVectorsResponse getVectorsResponse = vectorsClient.getVectors(b -> b.vectorBucketName(bucketName)
          .indexName(indexName).keys("key1", "key2", "key3").returnData(true).returnMetadata(true));

      GetOutputVector vector1 = getVectorsResponse.vectors().stream().filter(it -> it.key().equals("key1")).findFirst().get();
      assertEquals(vector1Data, vector1.data().float32());

      GetOutputVector vector2 = getVectorsResponse.vectors().stream().filter(it -> it.key().equals("key2")).findFirst().get();
      assertEquals(vector2Data, vector2.data().float32());

      GetOutputVector vector3 = getVectorsResponse.vectors().stream().filter(it -> it.key().equals("key3")).findFirst().get();
      assertEquals(vector3Data, vector3.data().float32());
      assertEquals(new MapDocument(new HashMap<String, Document>() {{
        put("meta1", new NumberDocument(SdkNumber.fromInteger(6)));
        put("meta2", new StringDocument("value"));
      }}), vector3.metadata());

      vectorsClient.deleteVectors(b -> b.vectorBucketName(bucketName).indexName(indexName).keys("key1"));
      GetVectorsResponse getVectorsResponse2 = vectorsClient.getVectors(b -> b.vectorBucketName(bucketName)
          .indexName(indexName).keys("key1", "key2", "key3"));
      assertEquals(2, getVectorsResponse2.vectors().size());
    } finally {
      vectorsClient.deleteIndex(b -> b.vectorBucketName(bucketName).indexName(indexName));
      vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
    }
  }

}
