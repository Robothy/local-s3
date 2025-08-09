package com.robothy.s3.rest.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class ResponseUtilsTest {

  @Test
  void testRFC1123Date() {
    String date = "Thu, 7 Aug 2025 14:37:13 GMT";
    assertEquals("Thu, 07 Aug 2025 14:37:13 GMT",
        ResponseUtils.RFC_1123_DATE_TIME.format(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date)));
  }

}