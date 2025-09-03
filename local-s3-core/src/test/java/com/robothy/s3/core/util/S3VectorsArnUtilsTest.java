package com.robothy.s3.core.util;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import org.junit.jupiter.api.Test;

class S3VectorsArnUtilsTest {

  @Test
  void resolveBucketName_withBucketName_returnsBucketName() {
    String result = S3VectorsArnUtils.resolveBucketName("my-bucket", null);
    
    assertEquals("my-bucket", result);
  }

  @Test
  void resolveBucketName_withBucketNameAndArn_prioritizesBucketName() {
    String result = S3VectorsArnUtils.resolveBucketName("my-bucket", 
        "arn:aws:s3vectors:::vector-bucket/other-bucket");
    
    assertEquals("my-bucket", result);
  }

  @Test
  void resolveBucketName_withValidArn_extractsFromArn() {
    String result = S3VectorsArnUtils.resolveBucketName(null, 
        "arn:aws:s3vectors:::vector-bucket/my-bucket");
    
    assertEquals("my-bucket", result);
  }

  @Test
  void resolveBucketName_withBucketNameHavingWhitespace_trimsWhitespace() {
    String result = S3VectorsArnUtils.resolveBucketName("  my-bucket  ", null);
    
    assertEquals("my-bucket", result);
  }

  @Test
  void resolveBucketName_withBothNull_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.resolveBucketName(null, null));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Either vectorBucketName or vectorBucketArn must be provided", exception.getMessage());
  }

  @Test
  void resolveBucketName_withBothEmpty_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.resolveBucketName("", ""));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Either vectorBucketName or vectorBucketArn must be provided", exception.getMessage());
  }

  @Test
  void resolveBucketName_withBothWhitespace_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.resolveBucketName("   ", "   "));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Either vectorBucketName or vectorBucketArn must be provided", exception.getMessage());
  }

  @Test
  void extractBucketNameFromArn_withValidArn_returnsBucketName() {
    String result = S3VectorsArnUtils.extractBucketNameFromArn(
        "arn:aws:s3vectors:::vector-bucket/test-bucket");
    
    assertEquals("test-bucket", result);
  }

  @Test
  void extractBucketNameFromArn_withBucketNameWithHyphens_returnsBucketName() {
    String result = S3VectorsArnUtils.extractBucketNameFromArn(
        "arn:aws:s3vectors:::vector-bucket/my-test-bucket-123");
    
    assertEquals("my-test-bucket-123", result);
  }

  @Test
  void extractBucketNameFromArn_withNullArn_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractBucketNameFromArn(null));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid vector bucket ARN format", exception.getMessage());
  }

  @Test
  void extractBucketNameFromArn_withInvalidPrefix_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractBucketNameFromArn("arn:aws:s3:::bucket/test"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid vector bucket ARN format", exception.getMessage());
  }

  @Test
  void extractBucketNameFromArn_withEmptyString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractBucketNameFromArn(""));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid vector bucket ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withValidArn_returnsBucketAndIndexNames() {
    String[] result = S3VectorsArnUtils.extractFromIndexArn(
        "arn:aws:s3vectors:::vector-bucket/test-bucket/index/test-index");
    
    assertEquals(2, result.length);
    assertEquals("test-bucket", result[0]);
    assertEquals("test-index", result[1]);
  }

  @Test
  void extractFromIndexArn_withComplexNames_returnsBucketAndIndexNames() {
    String[] result = S3VectorsArnUtils.extractFromIndexArn(
        "arn:aws:s3vectors:::vector-bucket/my-test-bucket-123/index/my-test-index_456");
    
    assertEquals(2, result.length);
    assertEquals("my-test-bucket-123", result[0]);
    assertEquals("my-test-index_456", result[1]);
  }

  @Test
  void extractFromIndexArn_withNullArn_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn(null));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withInvalidPrefix_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn("arn:aws:s3:::bucket/test/index/test"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withMissingIndexSegment_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn("arn:aws:s3vectors:::vector-bucket/test-bucket"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withMissingIndexName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn("arn:aws:s3vectors:::vector-bucket/test-bucket/index/"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withExtraSegments_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn(
            "arn:aws:s3vectors:::vector-bucket/test-bucket/index/test-index/extra"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void extractFromIndexArn_withEmptyString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> S3VectorsArnUtils.extractFromIndexArn(""));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }
}
