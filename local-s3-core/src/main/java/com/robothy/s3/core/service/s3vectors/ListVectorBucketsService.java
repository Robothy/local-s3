package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.util.vectors.CollectionProcessingUtils;
import com.robothy.s3.core.util.vectors.PaginationUtils;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.VectorBucket;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorBucketsResponse;
import com.robothy.s3.datatypes.s3vectors.response.VectorBucketSummary;

import java.util.List;
import java.util.stream.Collectors;

public interface ListVectorBucketsService extends S3VectorsMetadataAware {

  default ListVectorBucketsResponse listVectorBuckets(Integer maxResults, String nextToken, String prefix) {
    int normalizedMaxResults = validateRequestParameters(maxResults, prefix);
    int startIndex = PaginationUtils.parseNextToken(nextToken);
    
    List<VectorBucketMetadata> allBuckets = getAllVectorBuckets();
    List<VectorBucketMetadata> filteredBuckets = applyFiltersAndSorting(allBuckets, prefix);
    List<VectorBucketMetadata> paginatedResults = paginateResults(filteredBuckets, startIndex, normalizedMaxResults);
    
    List<VectorBucketSummary> summaries = convertToSummaries(paginatedResults);
    String responseNextToken = generateNextTokenIfNeeded(startIndex, paginatedResults.size(), filteredBuckets.size());
    
    return buildResponse(summaries, responseNextToken);
  }

  private int validateRequestParameters(Integer maxResults, String prefix) {
    ValidationUtils.validatePrefix(prefix);
    return PaginationUtils.validateAndNormalizeMaxResults(maxResults);
  }

  private List<VectorBucketMetadata> getAllVectorBuckets() {
    return metadata().listVectorBuckets();
  }

  private List<VectorBucketMetadata> applyFiltersAndSorting(List<VectorBucketMetadata> buckets, String prefix) {
    List<VectorBucketMetadata> filtered = CollectionProcessingUtils.applyPrefixFilter(
        buckets, prefix, VectorBucketMetadata::getVectorBucketName);
    return CollectionProcessingUtils.sortByName(filtered, VectorBucketMetadata::getVectorBucketName);
  }

  private List<VectorBucketMetadata> paginateResults(List<VectorBucketMetadata> buckets, int startIndex, int maxResults) {
    return CollectionProcessingUtils.applyPagination(buckets, startIndex, maxResults);
  }

  private List<VectorBucketSummary> convertToSummaries(List<VectorBucketMetadata> buckets) {
    return buckets.stream()
        .map(this::createVectorBucketSummary)
        .collect(Collectors.toList());
  }

  private VectorBucketSummary createVectorBucketSummary(VectorBucketMetadata bucketMetadata) {
    return VectorBucketSummary.builder()
        .vectorBucketName(bucketMetadata.getVectorBucketName())
        .vectorBucketArn(VectorBucket.generateArn(bucketMetadata.getVectorBucketName()))
        .creationTime(bucketMetadata.getCreationDate())
        .build();
  }

  private String generateNextTokenIfNeeded(int startIndex, int pageSize, int totalSize) {
    return PaginationUtils.generateNextToken(startIndex, pageSize, totalSize);
  }

  private ListVectorBucketsResponse buildResponse(List<VectorBucketSummary> summaries, String nextToken) {
    return ListVectorBucketsResponse.builder()
        .vectorBuckets(summaries)
        .nextToken(nextToken)
        .build();
  }

}
