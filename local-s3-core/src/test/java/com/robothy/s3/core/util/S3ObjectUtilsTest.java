package com.robothy.s3.core.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class S3ObjectUtilsTest {

  @CsvSource({
      "a,a",
      "a/b,a/b",
      "a/#b,a/%23b",
      "/a@,/a%40",
      "a@/b,a%40/b",
      "/a/,/a/"
  })
  @ParameterizedTest
  void urlEncodeEscapeSlash(String input, String expected) {
    assertEquals(expected, S3ObjectUtils.urlEncodeEscapeSlash(input));
  }

}