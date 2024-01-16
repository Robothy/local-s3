package com.robothy.s3.rest.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RequestUtilsTest {

  @Test
  void testExtractTagging() {

    HttpRequest request = mock(HttpRequest.class);
    when(request.header(AmzHeaderNames.X_AMZ_TAGGING)).thenReturn(Optional.of("key1=value1&key2=value2"));
    String[][] tagArray = RequestUtils.extractTagging(request).orElse(null);
    assertNotNull(tagArray);
    assertEquals(2, tagArray.length);
    assertEquals("key1", tagArray[0][0]);
    assertEquals("value1", tagArray[0][1]);
    assertEquals("key2", tagArray[1][0]);
    assertEquals("value2", tagArray[1][1]);


    when(request.header(AmzHeaderNames.X_AMZ_TAGGING)).thenReturn(Optional.of("key1=value1"));
    tagArray = RequestUtils.extractTagging(request).orElse(null);
    assertNotNull(tagArray);
    assertEquals(1, tagArray.length);
    assertEquals("key1", tagArray[0][0]);
    assertEquals("value1", tagArray[0][1]);

    when(request.header(AmzHeaderNames.X_AMZ_TAGGING)).thenReturn(Optional.of("invalid"));
    assertThrows(LocalS3InvalidArgumentException.class, () -> RequestUtils.extractTagging(request));
  }
}