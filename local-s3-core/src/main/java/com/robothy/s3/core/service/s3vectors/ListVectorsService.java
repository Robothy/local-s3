package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.datatypes.s3vectors.ListOutputVector;
import com.robothy.s3.datatypes.s3vectors.request.PutInputVector;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorsResponse;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface ListVectorsService extends S3VectorsMetadataAware, S3VectorsStorageAware {

    default ListVectorsResponse listVectors(String vectorBucketName, String indexName,
                                           Integer maxResults, String nextToken, Boolean returnData,
                                           Boolean returnMetadata, Integer segmentCount, Integer segmentIndex) {
        VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
        VectorIndexMetadata indexMetadata = VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
        
        int maxResultsValue = validateAndSetMaxResults(maxResults);
        validateSegmentParameters(segmentCount, segmentIndex);
        
        List<VectorObjectMetadata> sortedVectors = getSortedVectors(indexMetadata);
        List<VectorObjectMetadata> segmentVectors = applySegmentation(sortedVectors, segmentCount, segmentIndex);
        
        int startIndex = parseNextToken(nextToken);
        List<VectorObjectMetadata> pageVectors = applyPagination(segmentVectors, startIndex, maxResultsValue);
        
        List<ListOutputVector> outputVectors = buildOutputVectors(pageVectors, returnData, returnMetadata);
        String responseNextToken = calculateNextToken(startIndex, pageVectors.size(), segmentVectors.size());
        
        return ListVectorsResponse.builder()
            .vectors(outputVectors)
            .nextToken(responseNextToken)
            .build();
    }

    private int validateAndSetMaxResults(Integer maxResults) {
        return (maxResults != null && maxResults > 0) ? Math.min(maxResults, 1000) : 500;
    }

    private void validateSegmentParameters(Integer segmentCount, Integer segmentIndex) {
        if (segmentCount != null || segmentIndex != null) {
            if (segmentCount == null || segmentIndex == null) {
                throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                    "Both segmentCount and segmentIndex must be provided together");
            }
            if (segmentCount < 1 || segmentCount > 16) {
                throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                    "segmentCount must be between 1 and 16");
            }
            if (segmentIndex < 0 || segmentIndex >= segmentCount) {
                throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                    "segmentIndex must be between 0 and segmentCount-1");
            }
        }
    }

    private List<VectorObjectMetadata> getSortedVectors(VectorIndexMetadata indexMetadata) {
        Collection<VectorObjectMetadata> allVectors = indexMetadata.getVectorObjects().values();
        return allVectors.stream()
            .sorted(Comparator.comparing(VectorObjectMetadata::getVectorId))
            .collect(Collectors.toList());
    }

    private List<VectorObjectMetadata> applySegmentation(List<VectorObjectMetadata> allVectors,
                                                        Integer segmentCount, Integer segmentIndex) {
        if (segmentCount == null || segmentIndex == null) {
            return allVectors;
        }
        
        List<VectorObjectMetadata> segmentVectors = new ArrayList<>();
        for (int i = segmentIndex; i < allVectors.size(); i += segmentCount) {
            segmentVectors.add(allVectors.get(i));
        }
        return segmentVectors;
    }

    private int parseNextToken(String nextToken) {
        if (nextToken == null || nextToken.trim().isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(new String(Base64.getDecoder().decode(nextToken)));
        } catch (Exception e) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Invalid nextToken");
        }
    }

    private List<VectorObjectMetadata> applyPagination(List<VectorObjectMetadata> vectors, int startIndex, int maxResults) {
        return vectors.stream()
            .skip(startIndex)
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    private List<ListOutputVector> buildOutputVectors(List<VectorObjectMetadata> vectors, 
                                                     Boolean returnData, Boolean returnMetadata) {
        List<ListOutputVector> outputVectors = new ArrayList<>();
        
        for (VectorObjectMetadata vectorMetadata : vectors) {
            ListOutputVector outputVector = createOutputVector(vectorMetadata, returnData, returnMetadata);
            outputVectors.add(outputVector);
        }
        
        return outputVectors;
    }

    private ListOutputVector createOutputVector(VectorObjectMetadata vectorMetadata, 
                                               Boolean returnData, Boolean returnMetadata) {
        ListOutputVector.ListOutputVectorBuilder builder = ListOutputVector.builder()
            .key(vectorMetadata.getVectorId());

        if (shouldIncludeData(returnData)) {
            addVectorData(builder, vectorMetadata);
        }

        if (shouldIncludeMetadata(returnMetadata)) {
            builder.metadata(vectorMetadata.getMetadata());
        }

        return builder.build();
    }

    private boolean shouldIncludeData(Boolean returnData) {
        return Boolean.TRUE.equals(returnData);
    }

    private boolean shouldIncludeMetadata(Boolean returnMetadata) {
        return Boolean.TRUE.equals(returnMetadata);
    }

    private void addVectorData(ListOutputVector.ListOutputVectorBuilder builder, VectorObjectMetadata vectorMetadata) {
        try {
            float[] vectorData = vectorStorage().getVectorData(vectorMetadata.getStorageId());
            builder.data(PutInputVector.VectorData.builder().values(vectorData).build());
        } catch (Exception e) {
            // Continue without data on error
        }
    }

    private String calculateNextToken(int startIndex, int pageSize, int totalSize) {
        int nextStartIndex = startIndex + pageSize;
        if (nextStartIndex < totalSize) {
            return Base64.getEncoder().encodeToString(String.valueOf(nextStartIndex).getBytes());
        }
        return null;
    }
}
