package com.robothy.s3.core.utils.s3vectors;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;

/**
 * Utility class for validating vector bucket parameters.
 * Provides common validation logic for S3 vectors operations.
 */
public final class VectorBucketValidationUtils {

    private VectorBucketValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that the vector bucket name is provided and follows proper format.
     *
     * @param bucketName the bucket name to validate
     * @throws LocalS3VectorException if bucket name is null, empty, or invalid format
     */
    public static void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new LocalS3VectorException(
                LocalS3VectorErrorType.INVALID_REQUEST,
                "Vector bucket name is required"
            );
        }
        // Reuse existing bucket validation from LocalS3
        BucketAssertions.assertBucketNameIsValid(bucketName);
    }

    /**
     * Validates that at least one bucket identifier (name or ARN) is provided.
     *
     * @param bucketName the bucket name (may be null)
     * @param bucketArn the bucket ARN (may be null)
     * @throws LocalS3VectorException if both parameters are null or empty
     */
    public static void validateBucketIdentifier(String bucketName, String bucketArn) {
        if ((bucketName == null || bucketName.trim().isEmpty()) && 
            (bucketArn == null || bucketArn.trim().isEmpty())) {
            throw new LocalS3VectorException(
                LocalS3VectorErrorType.INVALID_REQUEST,
                "Either vector bucket name or ARN must be provided"
            );
        }
    }

    /**
     * Validates that the index name is provided and not empty.
     *
     * @param indexName the index name to validate
     * @throws LocalS3VectorException if index name is null or empty
     */
    public static void validateIndexName(String indexName) {
        if (indexName == null || indexName.trim().isEmpty()) {
            throw new LocalS3VectorException(
                LocalS3VectorErrorType.INVALID_REQUEST,
                "Index name is required"
            );
        }
    }
}
