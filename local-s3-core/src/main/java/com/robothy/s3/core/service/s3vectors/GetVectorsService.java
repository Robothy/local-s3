package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.GetVectorsResponse;
import com.robothy.s3.datatypes.s3vectors.response.GetOutputVector;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import java.util.ArrayList;
import java.util.List;

public interface GetVectorsService extends S3VectorsMetadataAware, S3VectorsStorageAware {

    default GetVectorsResponse getVectors(String vectorBucketName, String indexName,
                                         List<String> keys, Boolean returnData, Boolean returnMetadata) {
        VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
        VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
        validateKeys(keys);
        
        List<GetOutputVector> vectors = new ArrayList<>();
        List<String> errorVectorKeys = new ArrayList<>();
        
        processVectorKeys(keys, indexMetadata, returnData, returnMetadata, vectors, errorVectorKeys);
        
        return buildResponse(vectors, errorVectorKeys);
    }

    private void validateKeys(List<String> keys) {
        ValidationUtils.validateNotNullOrEmpty(keys, "Keys list cannot be null or empty");
        if (keys.size() > 100) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Cannot get more than 100 vectors at once");
        }
    }

    private void processVectorKeys(List<String> keys, VectorIndexMetadata indexMetadata, 
                                  Boolean returnData, Boolean returnMetadata,
                                  List<GetOutputVector> vectors, List<String> errorVectorKeys) {
        for (String key : keys) {
            try {
                processVectorKey(key, indexMetadata, returnData, returnMetadata, vectors);
            } catch (Exception e) {
                errorVectorKeys.add(key);
            }
        }
    }

    private void processVectorKey(String key, VectorIndexMetadata indexMetadata, 
                                 Boolean returnData, Boolean returnMetadata,
                                 List<GetOutputVector> vectors) {
        VectorObjectMetadata vectorMetadata = findVectorMetadata(key, indexMetadata);
        if (vectorMetadata == null) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND, "Vector not found: " + key);
        }

        GetOutputVector vector = createOutputVector(key, vectorMetadata, returnData, returnMetadata);
        vectors.add(vector);
    }

    private VectorObjectMetadata findVectorMetadata(String key, VectorIndexMetadata indexMetadata) {
        return indexMetadata.getVectorObject(key);
    }

    private GetOutputVector createOutputVector(String key, VectorObjectMetadata vectorMetadata,
                                              Boolean returnData, Boolean returnMetadata) {
        GetOutputVector vector = new GetOutputVector();
        vector.setKey(key);

        if (shouldIncludeData(returnData)) {
            addVectorData(vector, vectorMetadata);
        }

        if (shouldIncludeMetadata(returnMetadata)) {
            addVectorMetadata(vector, vectorMetadata);
        }

        return vector;
    }

    private boolean shouldIncludeData(Boolean returnData) {
        return Boolean.TRUE.equals(returnData);
    }

    private boolean shouldIncludeMetadata(Boolean returnMetadata) {
        return Boolean.TRUE.equals(returnMetadata);
    }

    private void addVectorData(GetOutputVector vector, VectorObjectMetadata vectorMetadata) {
        Long storageId = vectorMetadata.getStorageId();
        if (storageId != null) {
            float[] vectorData = vectorStorage().getVectorData(storageId);
            if (vectorData != null) {
                PutInputVector.VectorData data = new PutInputVector.VectorData();
                data.setValues(vectorData);
                vector.setData(data);
            }
        }
    }

    private void addVectorMetadata(GetOutputVector vector, VectorObjectMetadata vectorMetadata) {
        if (vectorMetadata.getMetadata() != null) {
            vector.setMetadata(vectorMetadata.getMetadata());
        }
    }

    private GetVectorsResponse buildResponse(List<GetOutputVector> vectors, List<String> errorVectorKeys) {
        GetVectorsResponse response = new GetVectorsResponse();
        response.setVectors(vectors);

        if (!errorVectorKeys.isEmpty()) {
            response.setErrorVectorKeys(errorVectorKeys);
        }

        return response;
    }
}
