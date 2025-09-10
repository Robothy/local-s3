package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.PutVectorsResponse;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import java.util.ArrayList;
import java.util.List;

public interface PutVectorsService extends S3VectorsMetadataAware, S3VectorsStorageAware {

  @BucketChanged
  default PutVectorsResponse putVectors(String vectorBucketName, String indexName,
      List<PutInputVector> vectors) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
    validateVectors(vectors);

    List<String> errorVectorKeys = new ArrayList<>();
    List<String> successfulVectorKeys = new ArrayList<>();

    processVectors(vectors, indexMetadata, errorVectorKeys, successfulVectorKeys);

    return buildResponse(errorVectorKeys);
  }

  private void validateVectors(List<PutInputVector> vectors) {
    ValidationUtils.validateNotNullOrEmpty(vectors, "Vectors list cannot be null or empty");
    if (vectors.size() > 500) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Cannot put more than 500 vectors at once");
    }
  }

  private void processVectors(List<PutInputVector> vectors, VectorIndexMetadata indexMetadata,
      List<String> errorVectorKeys, List<String> successfulVectorKeys) {
    for (PutInputVector inputVector : vectors) {
      try {
        processVector(inputVector, indexMetadata);
        successfulVectorKeys.add(inputVector.getKey());
      } catch (Exception e) {
        errorVectorKeys.add(inputVector.getKey());
      }
    }
  }

  private void processVector(PutInputVector inputVector, VectorIndexMetadata indexMetadata) {
    validateVectorInput(inputVector);
    float[] vectorData = extractVectorData(inputVector);
    validateVectorDimension(vectorData, indexMetadata.getDimension());

    Long storageId = storeVectorData(vectorData);
    VectorObjectMetadata vectorMetadata = createVectorMetadata(inputVector, vectorData, storageId);

    indexMetadata.addVectorObject(vectorMetadata);
  }

  private void validateVectorInput(PutInputVector inputVector) {
    ValidationUtils.validateNotBlank(inputVector.getKey(), "Vector key is required");

    if (inputVector.getData() == null || inputVector.getData().getValues() == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Vector data is required");
    }
  }

  private float[] extractVectorData(PutInputVector inputVector) {
    float[] vectorData = inputVector.getData().getValues();
    if (vectorData.length == 0) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Vector data cannot be empty");
    }
    return vectorData;
  }

  private void validateVectorDimension(float[] vectorData, int expectedDimension) {
    if (vectorData.length != expectedDimension) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          String.format("Vector dimension %d does not match index dimension %d",
              vectorData.length, expectedDimension));
    }
  }

  private Long storeVectorData(float[] vectorData) {
    return vectorStorage().putVectorData(vectorData);
  }

  private VectorObjectMetadata createVectorMetadata(PutInputVector inputVector, float[] vectorData, Long storageId) {
    VectorObjectMetadata vectorMetadata = new VectorObjectMetadata();
    vectorMetadata.setVectorId(inputVector.getKey());
    vectorMetadata.setDimension(vectorData.length);
    vectorMetadata.setStorageId(storageId);
    vectorMetadata.setCreationDate(System.currentTimeMillis());

    if (hasMetadata(inputVector)) {
      vectorMetadata.setMetadata(inputVector.getMetadata());
    }

    return vectorMetadata;
  }

  private boolean hasMetadata(PutInputVector inputVector) {
    return inputVector.getMetadata() != null && !inputVector.getMetadata().isNull();
  }

  private PutVectorsResponse buildResponse(List<String> errorVectorKeys) {
    PutVectorsResponse response = new PutVectorsResponse();

    if (!errorVectorKeys.isEmpty()) {
      response.setErrorVectorKeys(errorVectorKeys);
    }

    return response;
  }
}
