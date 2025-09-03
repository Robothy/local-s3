package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.datatypes.s3vectors.VectorIndex;
import com.robothy.s3.datatypes.s3vectors.request.CreateIndexRequest;
import com.robothy.s3.datatypes.s3vectors.response.GetIndexResponse;

public interface GetIndexService extends S3VectorsMetadataAware {

  default GetIndexResponse getIndex(String vectorBucketName, String indexName) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
    VectorIndex vectorIndex = buildVectorIndex(indexMetadata, vectorBucketName);
    return buildResponse(vectorIndex);
  }

  private VectorIndex buildVectorIndex(VectorIndexMetadata indexMetadata, String bucketName) {
    CreateIndexRequest.MetadataConfiguration metadataConfig = buildMetadataConfiguration(indexMetadata);

    return VectorIndex.builder()
        .indexName(indexMetadata.getIndexName())
        .indexArn(VectorIndex.generateArn(bucketName, indexMetadata.getIndexName()))
        .dimension(indexMetadata.getDimension())
        .dataType(indexMetadata.getDataType())
        .distanceMetric(indexMetadata.getDistanceMetric())
        .metadataConfiguration(metadataConfig)
        .creationTime(convertToUnixTimestamp(indexMetadata.getCreationDate()))
        .vectorBucketName(bucketName)
        .status(indexMetadata.getStatus())
        .vectorCount((long) indexMetadata.getVectorObjectCount())
        .build();
  }

  private CreateIndexRequest.MetadataConfiguration buildMetadataConfiguration(VectorIndexMetadata indexMetadata) {
    if (indexMetadata.getNonFilterableMetadataKeys() != null && !indexMetadata.getNonFilterableMetadataKeys().isEmpty()) {
      return CreateIndexRequest.MetadataConfiguration.builder()
          .nonFilterableMetadataKeys(indexMetadata.getNonFilterableMetadataKeys())
          .build();
    }
    return null;
  }

  private long convertToUnixTimestamp(long timestampMillis) {
    return timestampMillis / 1000;
  }

  private GetIndexResponse buildResponse(VectorIndex vectorIndex) {
    return GetIndexResponse.builder()
        .index(vectorIndex)
        .build();
  }
}
