package com.robothy.s3.core.service.s3vectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import com.robothy.s3.datatypes.s3vectors.response.QueryVectorsResponse;
import com.robothy.s3.datatypes.s3vectors.response.QueryOutputVector;
import java.util.ArrayList;
import java.util.List;

public interface QueryVectorsService extends S3VectorsMetadataAware, S3VectorsStorageAware {

    default QueryVectorsResponse queryVectors(String vectorBucketName, String indexName,
                                             PutInputVector.VectorData queryVector, Integer topK,
                                             Boolean returnDistance, Boolean returnMetadata, 
                                             JsonNode filter) {
        VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
        VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
        float[] queryVectorData = validateQueryVector(queryVector, indexMetadata.getDimension());
        int validatedTopK = validateTopK(topK);

        List<VectorObjectMetadata> candidateVectors = getCandidateVectors(indexMetadata);
        
        if (candidateVectors.isEmpty()) {
            return buildEmptyResponse();
        }

        List<VectorSearchEngine.VectorSearchResult> searchResults = performVectorSearch(
            queryVectorData, candidateVectors, indexMetadata, validatedTopK, filter);
        
        List<QueryOutputVector> outputVectors = buildOutputVectors(searchResults, returnDistance, returnMetadata);
        
        return buildResponse(outputVectors);
    }

    private float[] validateQueryVector(PutInputVector.VectorData queryVector, int indexDimension) {
        if (queryVector == null || queryVector.getValues() == null) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Query vector is required");
        }

        float[] queryVectorData = queryVector.getValues();
        if (queryVectorData.length == 0) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Query vector data cannot be empty");
        }

        if (queryVectorData.length != indexDimension) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
                String.format("Query vector dimension %d does not match index dimension %d", 
                             queryVectorData.length, indexDimension));
        }

        return queryVectorData;
    }

    private int validateTopK(Integer topK) {
        if (topK == null || topK < 1) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "topK must be at least 1");
        }
        return topK;
    }

    private List<VectorObjectMetadata> getCandidateVectors(VectorIndexMetadata indexMetadata) {
        return new ArrayList<>(indexMetadata.getVectorObjects().values());
    }

    private QueryVectorsResponse buildEmptyResponse() {
        return QueryVectorsResponse.builder()
            .vectors(new ArrayList<>())
            .build();
    }

    private List<VectorSearchEngine.VectorSearchResult> performVectorSearch(
            float[] queryVectorData, List<VectorObjectMetadata> candidateVectors,
            VectorIndexMetadata indexMetadata, int topK, JsonNode filter) {
        VectorSearchEngine searchEngine = VectorSearchEngine.createBasic();
        return searchEngine.findNearestVectors(
            queryVectorData,
            candidateVectors,
            vectorStorage(),
            indexMetadata.getDistanceMetric(),
            Math.min(topK, candidateVectors.size()),
            filter
        );
    }

    private List<QueryOutputVector> buildOutputVectors(List<VectorSearchEngine.VectorSearchResult> searchResults,
                                                      Boolean returnDistance, Boolean returnMetadata) {
        List<QueryOutputVector> outputVectors = new ArrayList<>();
        
        for (VectorSearchEngine.VectorSearchResult result : searchResults) {
            QueryOutputVector outputVector = createOutputVector(result, returnDistance, returnMetadata);
            outputVectors.add(outputVector);
        }
        
        return outputVectors;
    }

    private QueryOutputVector createOutputVector(VectorSearchEngine.VectorSearchResult result,
                                                Boolean returnDistance, Boolean returnMetadata) {
        VectorObjectMetadata vectorMetadata = result.vectorMetadata();
        
        QueryOutputVector.QueryOutputVectorBuilder builder = QueryOutputVector.builder()
            .key(vectorMetadata.getVectorId());

        if (shouldReturnDistance(returnDistance)) {
            builder.distance(result.distance());
        }

        if (shouldReturnMetadata(returnMetadata, vectorMetadata)) {
            builder.metadata(vectorMetadata.getMetadata());
        }

        return builder.build();
    }

    private boolean shouldReturnDistance(Boolean returnDistance) {
        return Boolean.TRUE.equals(returnDistance);
    }

    private boolean shouldReturnMetadata(Boolean returnMetadata, VectorObjectMetadata vectorMetadata) {
        return Boolean.TRUE.equals(returnMetadata) && vectorMetadata.getMetadata() != null;
    }

    private QueryVectorsResponse buildResponse(List<QueryOutputVector> outputVectors) {
        return QueryVectorsResponse.builder()
            .vectors(outputVectors)
            .build();
    }
}
