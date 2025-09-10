package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.DistanceMetric;
import com.robothy.s3.datatypes.s3vectors.VectorDataType;
import com.robothy.s3.datatypes.s3vectors.VectorIndex;
import com.robothy.s3.datatypes.s3vectors.response.CreateIndexResponse;
import java.util.ArrayList;
import java.util.List;

public interface CreateIndexService extends S3VectorsMetadataAware {

  @BucketChanged
  default CreateIndexResponse createIndex(String vectorBucketName, String indexName,
      VectorDataType dataType, int dimension, DistanceMetric distanceMetric,
      List<String> nonFilterableMetadataKeys) {

    validateIndexParameters(indexName, dimension, dataType, distanceMetric);

    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    VectorIndexAssertions.assertVectorIndexNotExists(bucketMetadata, indexName);

    VectorIndexMetadata indexMetadata =
        createIndexMetadata(indexName, dimension, dataType, distanceMetric, nonFilterableMetadataKeys);
    bucketMetadata.putIndexMetadata(indexName, indexMetadata);

    return CreateIndexResponse.builder().build();
  }

  private void validateIndexParameters(String indexName, int dimension, VectorDataType dataType, DistanceMetric distanceMetric) {
    ValidationUtils.validateNotBlank(indexName, "Index name is required");
    VectorIndex.validateDimension(dimension);
    validateDataType(dataType);
    validateDistanceMetric(distanceMetric);
  }

  private void validateDataType(VectorDataType dataType) {
    if (dataType == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Data type is required");
    }
    if (dataType != VectorDataType.FLOAT32) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Only FLOAT32 data type is currently supported");
    }
  }

  private void validateDistanceMetric(DistanceMetric distanceMetric) {
    if (distanceMetric == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Distance metric is required");
    }
  }

  private VectorIndexMetadata createIndexMetadata(String indexName, int dimension, VectorDataType dataType,
      DistanceMetric distanceMetric, List<String> nonFilterableMetadataKeys) {
    VectorIndexMetadata indexMetadata = new VectorIndexMetadata();
    indexMetadata.setIndexName(indexName);
    indexMetadata.setDimension(dimension);
    indexMetadata.setDataType(dataType);
    indexMetadata.setDistanceMetric(distanceMetric);
    indexMetadata.setCreationDate(System.currentTimeMillis());
    indexMetadata.setActive();

    if (hasNonFilterableMetadataKeys(nonFilterableMetadataKeys)) {
      indexMetadata.setNonFilterableMetadataKeys(new ArrayList<>(nonFilterableMetadataKeys));
    }

    return indexMetadata;
  }

  private boolean hasNonFilterableMetadataKeys(List<String> nonFilterableMetadataKeys) {
    return nonFilterableMetadataKeys != null && !nonFilterableMetadataKeys.isEmpty();
  }
}
