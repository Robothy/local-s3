package com.robothy.s3.core.util.vectors;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class PaginationUtilsTest {

  @Test
  void validateAndNormalizeMaxResults_withNull_returnsDefault() {
    int result = PaginationUtils.validateAndNormalizeMaxResults(null);
    
    assertEquals(500, result);
  }

  @Test
  void validateAndNormalizeMaxResults_withValidValue_returnsValue() {
    int result = PaginationUtils.validateAndNormalizeMaxResults(100);
    
    assertEquals(100, result);
  }

  @Test
  void validateAndNormalizeMaxResults_withMinimumValue_returnsValue() {
    int result = PaginationUtils.validateAndNormalizeMaxResults(1);
    
    assertEquals(1, result);
  }

  @Test
  void validateAndNormalizeMaxResults_withMaximumValue_returnsValue() {
    int result = PaginationUtils.validateAndNormalizeMaxResults(500);
    
    assertEquals(500, result);
  }

  @Test
  void validateAndNormalizeMaxResults_withZero_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.validateAndNormalizeMaxResults(0));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("maxResults must be between 1 and 500", exception.getMessage());
  }

  @Test
  void validateAndNormalizeMaxResults_withNegativeValue_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.validateAndNormalizeMaxResults(-1));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("maxResults must be between 1 and 500", exception.getMessage());
  }

  @Test
  void validateAndNormalizeMaxResults_withValueTooLarge_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.validateAndNormalizeMaxResults(501));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("maxResults must be between 1 and 500", exception.getMessage());
  }

  @Test
  void parseNextToken_withNull_returnsZero() {
    int result = PaginationUtils.parseNextToken(null);
    
    assertEquals(0, result);
  }

  @Test
  void parseNextToken_withEmptyString_returnsZero() {
    int result = PaginationUtils.parseNextToken("");
    
    assertEquals(0, result);
  }

  @Test
  void parseNextToken_withWhitespaceOnly_returnsZero() {
    int result = PaginationUtils.parseNextToken("   ");
    
    assertEquals(0, result);
  }

  @Test
  void parseNextToken_withValidToken_returnsIndex() {
    String token = Base64.getEncoder().encodeToString("100".getBytes());
    
    int result = PaginationUtils.parseNextToken(token);
    
    assertEquals(100, result);
  }

  @Test
  void parseNextToken_withZeroIndex_returnsZero() {
    String token = Base64.getEncoder().encodeToString("0".getBytes());
    
    int result = PaginationUtils.parseNextToken(token);
    
    assertEquals(0, result);
  }

  @Test
  void parseNextToken_withInvalidBase64_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.parseNextToken("invalid-base64!!!"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid nextToken", exception.getMessage());
  }

  @Test
  void parseNextToken_withNonNumericContent_throwsException() {
    String token = Base64.getEncoder().encodeToString("not-a-number".getBytes());
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.parseNextToken(token));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Invalid nextToken", exception.getMessage());
  }

  @Test
  void parseNextToken_withTooLongToken_throwsException() {
    String longToken = "a".repeat(513);
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> PaginationUtils.parseNextToken(longToken));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("nextToken length must not exceed 512 characters", exception.getMessage());
  }

  @Test
  void parseNextToken_withMaxLengthToken_doesNotThrow() {
    String maxLengthContent = "a".repeat(384); // Base64 encoded will be 512 chars
    String token = Base64.getEncoder().encodeToString("100".getBytes());
    
    assertDoesNotThrow(() -> PaginationUtils.parseNextToken(token));
  }

  @Test
  void generateNextToken_withMoreResults_returnsToken() {
    String token = PaginationUtils.generateNextToken(0, 10, 20);
    
    assertNotNull(token);
    int decodedIndex = Integer.parseInt(new String(Base64.getDecoder().decode(token)));
    assertEquals(10, decodedIndex);
  }

  @Test
  void generateNextToken_withNoMoreResults_returnsNull() {
    String token = PaginationUtils.generateNextToken(10, 10, 20);
    
    assertNull(token);
  }

  @Test
  void generateNextToken_withExactlyAtEnd_returnsNull() {
    String token = PaginationUtils.generateNextToken(15, 5, 20);
    
    assertNull(token);
  }

  @Test
  void generateNextToken_withZeroCurrentIndex_returnsValidToken() {
    String token = PaginationUtils.generateNextToken(0, 5, 10);
    
    assertNotNull(token);
    int decodedIndex = Integer.parseInt(new String(Base64.getDecoder().decode(token)));
    assertEquals(5, decodedIndex);
  }

  @Test
  void generateNextToken_withLargeIndex_returnsValidToken() {
    String token = PaginationUtils.generateNextToken(1000, 100, 1500);
    
    assertNotNull(token);
    int decodedIndex = Integer.parseInt(new String(Base64.getDecoder().decode(token)));
    assertEquals(1100, decodedIndex);
  }

  @Test
  void parseNextToken_roundTripWithGeneratedToken_maintainsValue() {
    String originalToken = PaginationUtils.generateNextToken(50, 25, 100);
    
    int parsedIndex = PaginationUtils.parseNextToken(originalToken);
    
    assertEquals(75, parsedIndex);
  }

  @Test
  void validateAndNormalizeMaxResults_withLargeValue_clamsToMaximum() {
    int result = PaginationUtils.validateAndNormalizeMaxResults(1000);
    
    assertEquals(500, result);
  }

  @Test
  void generateNextToken_withSingleItemPage_returnsCorrectToken() {
    String token = PaginationUtils.generateNextToken(0, 1, 5);
    
    assertNotNull(token);
    int decodedIndex = Integer.parseInt(new String(Base64.getDecoder().decode(token)));
    assertEquals(1, decodedIndex);
  }
}
