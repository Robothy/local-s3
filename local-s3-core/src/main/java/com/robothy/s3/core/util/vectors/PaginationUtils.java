package com.robothy.s3.core.util.vectors;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;

import java.util.Base64;

/**
 * Utility class for handling pagination in S3 Vectors operations.
 * Provides validation and parsing for pagination parameters.
 */
public class PaginationUtils {

  private static final int DEFAULT_MAX_RESULTS = 500;
  private static final int MIN_MAX_RESULTS = 1;
  private static final int MAX_MAX_RESULTS = 500;
  private static final int MAX_TOKEN_LENGTH = 512;

  /**
   * Validates and normalizes maxResults parameter.
   * 
   * @param maxResults the requested maximum results
   * @return normalized maxResults value
   * @throws LocalS3VectorException if maxResults is out of valid range
   */
  public static int validateAndNormalizeMaxResults(Integer maxResults) {
    if (maxResults == null) {
      return DEFAULT_MAX_RESULTS;
    }
    
    if (maxResults < MIN_MAX_RESULTS || maxResults > MAX_MAX_RESULTS) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "maxResults must be between " + MIN_MAX_RESULTS + " and " + MAX_MAX_RESULTS);
    }
    
    return Math.min(maxResults, MAX_MAX_RESULTS);
  }

  /**
   * Validates and parses nextToken to extract start index.
   * 
   * @param nextToken the pagination token
   * @return parsed start index (0 if token is null/empty)
   * @throws LocalS3VectorException if token is invalid
   */
  public static int parseNextToken(String nextToken) {
    if (nextToken == null || nextToken.trim().isEmpty()) {
      return 0;
    }
    
    validateTokenLength(nextToken);
    return decodeTokenToIndex(nextToken);
  }

  /**
   * Generates a nextToken for pagination if there are more results.
   * 
   * @param currentStartIndex current pagination start index
   * @param pageSize size of current page
   * @param totalFilteredSize total number of filtered items
   * @return nextToken string or null if no more results
   */
  public static String generateNextToken(int currentStartIndex, int pageSize, int totalFilteredSize) {
    int nextStartIndex = currentStartIndex + pageSize;
    if (nextStartIndex >= totalFilteredSize) {
      return null;
    }
    
    return Base64.getEncoder().encodeToString(String.valueOf(nextStartIndex).getBytes());
  }

  private static void validateTokenLength(String nextToken) {
    if (nextToken.length() > MAX_TOKEN_LENGTH) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "nextToken length must not exceed " + MAX_TOKEN_LENGTH + " characters");
    }
  }

  private static int decodeTokenToIndex(String nextToken) {
    try {
      return Integer.parseInt(new String(Base64.getDecoder().decode(nextToken)));
    } catch (Exception e) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, "Invalid nextToken");
    }
  }
}
