package com.robothy.s3.core.util.vectors;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

  @Test
  void validatePrefix_withNullPrefix_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validatePrefix(null));
  }

  @Test
  void validatePrefix_withValidPrefix_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validatePrefix("valid-prefix"));
  }

  @Test
  void validatePrefix_withMinimumLength_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validatePrefix("a"));
  }

  @Test
  void validatePrefix_withMaximumLength_doesNotThrow() {
    String maxLengthPrefix = "a".repeat(63);
    assertDoesNotThrow(() -> ValidationUtils.validatePrefix(maxLengthPrefix));
  }

  @Test
  void validatePrefix_withEmptyString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validatePrefix(""));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("prefix length must be between 1 and 63 characters", exception.getMessage());
  }

  @Test
  void validatePrefix_withTooLongPrefix_throwsException() {
    String tooLongPrefix = "a".repeat(64);
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validatePrefix(tooLongPrefix));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("prefix length must be between 1 and 63 characters", exception.getMessage());
  }

  @Test
  void shouldApplyPrefixFilter_withNull_returnsFalse() {
    boolean result = ValidationUtils.shouldApplyPrefixFilter(null);
    
    assertFalse(result);
  }

  @Test
  void shouldApplyPrefixFilter_withEmptyString_returnsFalse() {
    boolean result = ValidationUtils.shouldApplyPrefixFilter("");
    
    assertFalse(result);
  }

  @Test
  void shouldApplyPrefixFilter_withWhitespaceOnly_returnsFalse() {
    boolean result = ValidationUtils.shouldApplyPrefixFilter("   ");
    
    assertFalse(result);
  }

  @Test
  void shouldApplyPrefixFilter_withValidPrefix_returnsTrue() {
    boolean result = ValidationUtils.shouldApplyPrefixFilter("valid-prefix");
    
    assertTrue(result);
  }

  @Test
  void shouldApplyPrefixFilter_withPrefixWithWhitespace_returnsTrue() {
    boolean result = ValidationUtils.shouldApplyPrefixFilter("  prefix  ");
    
    assertTrue(result);
  }

  @Test
  void validateNotNullOrEmpty_withNullList_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotNullOrEmpty(null, "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validateNotNullOrEmpty_withEmptyList_throwsException() {
    List<String> emptyList = new ArrayList<>();
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotNullOrEmpty(emptyList, "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validateNotNullOrEmpty_withNonEmptyList_doesNotThrow() {
    List<String> nonEmptyList = List.of("item1", "item2");
    
    assertDoesNotThrow(() -> ValidationUtils.validateNotNullOrEmpty(nonEmptyList, "Test message"));
  }

  @Test
  void validateNotNullOrEmpty_withSingleItemList_doesNotThrow() {
    List<String> singleItemList = List.of("item");
    
    assertDoesNotThrow(() -> ValidationUtils.validateNotNullOrEmpty(singleItemList, "Test message"));
  }

  @Test
  void validateNotBlank_withNullString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotBlank(null, "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validateNotBlank_withEmptyString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotBlank("", "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validateNotBlank_withWhitespaceOnlyString_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotBlank("   ", "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validateNotBlank_withValidString_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validateNotBlank("valid-string", "Test message"));
  }

  @Test
  void validateNotBlank_withStringWithWhitespace_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validateNotBlank("  valid-string  ", "Test message"));
  }

  @Test
  void validateNotBlank_withTabsAndNewlines_throwsException() {
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotBlank("\t\n\r ", "Test message"));
    
    assertEquals(LocalS3VectorErrorType.INVALID_REQUEST, exception.getErrorType());
    assertEquals("Test message", exception.getMessage());
  }

  @Test
  void validatePrefix_withSpecialCharacters_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validatePrefix("prefix-with_special.chars"));
  }

  @Test
  void validateNotBlank_withSingleCharacter_doesNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.validateNotBlank("a", "Test message"));
  }

  @Test
  void validateNotNullOrEmpty_withCustomMessage_usesProvidedMessage() {
    String customMessage = "Custom validation error message";
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotNullOrEmpty(null, customMessage));
    
    assertEquals(customMessage, exception.getMessage());
  }

  @Test
  void validateNotBlank_withCustomMessage_usesProvidedMessage() {
    String customMessage = "Custom blank validation error message";
    
    LocalS3VectorException exception = assertThrows(LocalS3VectorException.class,
        () -> ValidationUtils.validateNotBlank("", customMessage));
    
    assertEquals(customMessage, exception.getMessage());
  }
}
