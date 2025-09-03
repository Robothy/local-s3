package com.robothy.s3.core.model.internal.s3vectors;

import static org.junit.jupiter.api.Assertions.*;

import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for IndexIdentifier.
 */
class IndexIdentifierTest {

  @Test
  void constructor_withValidParameters_createsInstance() {
    IndexIdentifier identifier = new IndexIdentifier("test-bucket", "test-index");

    assertEquals("test-bucket", identifier.bucketName());
    assertEquals("test-index", identifier.indexName());
  }

  @Test
  void constructor_withBlankBucketName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier("", "test-index"));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Vector bucket name is required", exception.getMessage());
  }

  @Test
  void constructor_withNullBucketName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier(null, "test-index"));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Vector bucket name is required", exception.getMessage());
  }

  @Test
  void constructor_withWhitespaceBucketName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier("   ", "test-index"));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Vector bucket name is required", exception.getMessage());
  }

  @Test
  void constructor_withBlankIndexName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier("test-bucket", ""));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void constructor_withNullIndexName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier("test-bucket", null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void constructor_withWhitespaceIndexName_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> new IndexIdentifier("test-bucket", "   "));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void fromIndexArn_withValidArn_createsInstance() {
    String arn = "arn:aws:s3vectors:::vector-bucket/my-bucket/index/my-index";

    IndexIdentifier identifier = IndexIdentifier.fromIndexArn(arn);

    assertEquals("my-bucket", identifier.bucketName());
    assertEquals("my-index", identifier.indexName());
  }

  @Test
  void fromIndexArn_withComplexNames_createsInstance() {
    String arn = "arn:aws:s3vectors:::vector-bucket/test-bucket-123/index/vector-index-456";

    IndexIdentifier identifier = IndexIdentifier.fromIndexArn(arn);

    assertEquals("test-bucket-123", identifier.bucketName());
    assertEquals("vector-index-456", identifier.indexName());
  }

  @Test
  void fromIndexArn_withNullArn_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn(null));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void fromIndexArn_withInvalidArnFormat_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn("invalid-arn"));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void fromIndexArn_withIncompleteArn_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn("arn:aws:s3vectors:::vector-bucket/my-bucket"));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void fromIndexArn_withMissingBucketName_throwsException() {
    String arn = "arn:aws:s3vectors:::vector-bucket//index/my-index";

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn(arn));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Vector bucket name is required", exception.getMessage());
  }

  @Test
  void fromIndexArn_withMissingIndexName_throwsException() {
    String arn = "arn:aws:s3vectors:::vector-bucket/my-bucket/index/";

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn(arn));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid index ARN format", exception.getMessage());
  }

  @Test
  void fromIndexArn_withBlankBucketNameInArn_throwsException() {
    String arn = "arn:aws:s3vectors:::vector-bucket/   /index/my-index";

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn(arn));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Vector bucket name is required", exception.getMessage());
  }

  @Test
  void fromIndexArn_withBlankIndexNameInArn_throwsException() {
    String arn = "arn:aws:s3vectors:::vector-bucket/my-bucket/index/   ";

    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class, 
        () -> IndexIdentifier.fromIndexArn(arn));

    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Index name is required", exception.getMessage());
  }

  @Test
  void equals_withSameValues_returnsTrue() {
    IndexIdentifier identifier1 = new IndexIdentifier("bucket", "index");
    IndexIdentifier identifier2 = new IndexIdentifier("bucket", "index");

    assertEquals(identifier1, identifier2);
    assertEquals(identifier1.hashCode(), identifier2.hashCode());
  }

  @Test
  void equals_withDifferentValues_returnsFalse() {
    IndexIdentifier identifier1 = new IndexIdentifier("bucket1", "index1");
    IndexIdentifier identifier2 = new IndexIdentifier("bucket2", "index2");

    assertNotEquals(identifier1, identifier2);
  }

  @Test
  void toString_containsAllFields() {
    IndexIdentifier identifier = new IndexIdentifier("test-bucket", "test-index");

    String toString = identifier.toString();

    assertTrue(toString.contains("test-bucket"));
    assertTrue(toString.contains("test-index"));
  }
}
