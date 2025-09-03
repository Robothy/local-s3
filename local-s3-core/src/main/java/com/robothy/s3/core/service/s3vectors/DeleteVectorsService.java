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
import com.robothy.s3.datatypes.s3vectors.response.DeleteVectorsResponse;
import java.util.ArrayList;
import java.util.List;

public interface DeleteVectorsService extends S3VectorsMetadataAware, S3VectorsStorageAware {

  @BucketChanged
  default DeleteVectorsResponse deleteVectors(String vectorBucketName, String indexName,
      List<String> keys) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
    validateKeys(keys);

    List<String> deletedVectorKeys = new ArrayList<>();
    List<String> errorVectorKeys = new ArrayList<>();

    processVectorDeletions(keys, indexMetadata, deletedVectorKeys, errorVectorKeys);

    return buildResponse(deletedVectorKeys, errorVectorKeys);
  }

  private void validateKeys(List<String> keys) {
    ValidationUtils.validateNotNullOrEmpty(keys, "Keys list cannot be null or empty");
    if (keys.size() > 500) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Cannot delete more than 500 vectors at once");
    }
  }

  private void processVectorDeletions(List<String> keys, VectorIndexMetadata indexMetadata,
      List<String> deletedVectorKeys, List<String> errorVectorKeys) {
    for (String key : keys) {
      try {
        deleteVector(key, indexMetadata);
        deletedVectorKeys.add(key);
      } catch (Exception e) {
        errorVectorKeys.add(key);
      }
    }
  }

  private void deleteVector(String key, VectorIndexMetadata indexMetadata) {
    VectorObjectMetadata vectorMetadata = findVectorMetadata(key, indexMetadata);
    if (vectorMetadata == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND, "Vector not found: " + key);
    }

    deleteVectorFromStorage(vectorMetadata);
    removeVectorFromIndex(key, indexMetadata);
  }

  private VectorObjectMetadata findVectorMetadata(String key, VectorIndexMetadata indexMetadata) {
    return indexMetadata.getVectorObject(key);
  }

  private void deleteVectorFromStorage(VectorObjectMetadata vectorMetadata) {
    Long storageId = vectorMetadata.getStorageId();
    if (storageId != null) {
      boolean deletedFromStorage = vectorStorage().deleteVectorData(storageId);
      if (!deletedFromStorage) {
        // Continue with metadata cleanup even if storage deletion fails
      }
    }
  }

  private void removeVectorFromIndex(String key, VectorIndexMetadata indexMetadata) {
    indexMetadata.removeVectorObject(key);
  }

  private DeleteVectorsResponse buildResponse(List<String> deletedVectorKeys, List<String> errorVectorKeys) {
    DeleteVectorsResponse response = new DeleteVectorsResponse();

    if (!deletedVectorKeys.isEmpty()) {
      response.setDeletedVectorKeys(deletedVectorKeys);
    }

    if (!errorVectorKeys.isEmpty()) {
      response.setErrorVectorKeys(errorVectorKeys);
    }

    return response;
  }
}
