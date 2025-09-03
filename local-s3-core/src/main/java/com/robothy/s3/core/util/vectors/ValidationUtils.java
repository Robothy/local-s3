package com.robothy.s3.core.util.vectors;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import java.util.List;

/**
 * Utility class for validating S3 Vectors operation parameters.
 * Provides consistent validation logic for common parameters.
 */
public class ValidationUtils {

  private static final int MIN_PREFIX_LENGTH = 1;
  private static final int MAX_PREFIX_LENGTH = 63;

  /**
   * Validates prefix parameter for list operations.
   * 
   * @param prefix the prefix to validate
   * @throws LocalS3VectorException if prefix length is invalid
   */
  public static void validatePrefix(String prefix) {
    if (prefix == null) {
      return;
    }
    
    if (prefix.length() < MIN_PREFIX_LENGTH || prefix.length() > MAX_PREFIX_LENGTH) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "prefix length must be between " + MIN_PREFIX_LENGTH + " and " + MAX_PREFIX_LENGTH + " characters");
    }
  }

  /**
   * Checks if prefix filtering should be applied.
   * 
   * @param prefix the prefix parameter
   * @return true if prefix is non-null and non-empty
   */
  public static boolean shouldApplyPrefixFilter(String prefix) {
    return prefix != null && !prefix.trim().isEmpty();
  }

  /**
   * Validates that a list is not null or empty.
   * 
   * @param list the list to validate
   * @param message the error message if validation fails
   * @throws LocalS3VectorException if list is null or empty
   */
  public static void validateNotNullOrEmpty(List<?> list, String message) {
    if (list == null || list.isEmpty()) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, message);
    }
  }

  /**
   * Validates that a string is not null or blank.
   * 
   * @param value the string to validate
   * @param message the error message if validation fails
   * @throws LocalS3VectorException if string is null or blank
   */
  public static void validateNotBlank(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, message);
    }
  }
}
