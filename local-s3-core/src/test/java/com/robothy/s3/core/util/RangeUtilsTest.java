package com.robothy.s3.core.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class RangeUtilsTest {

  @Test
  void applyRangeSkipsPrefixAndReturnsRequestedLength() throws IOException {
    InputStream stream = new ByteArrayInputStream("Hello, World!".getBytes(StandardCharsets.UTF_8));
    byte[] bytes = RangeUtils.applyRange(stream, 7, 5).readAllBytes();
    assertArrayEquals("World".getBytes(StandardCharsets.UTF_8), bytes);
  }

  @Test
  void applyRangeHandlesNonProgressingSkip() throws IOException {
    InputStream stream = new ByteArrayInputStream("abcdef".getBytes(StandardCharsets.UTF_8)) {
      @Override
      public long skip(long n) {
        return 0;
      }
    };
    byte[] bytes = RangeUtils.applyRange(stream, 3, 2).readAllBytes();
    assertArrayEquals("ab".getBytes(StandardCharsets.UTF_8), bytes);
  }

  @Test
  void applyRangeWrapsIoException() {
    InputStream stream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("boom");
      }
    };
    assertThrows(IllegalStateException.class, () -> RangeUtils.applyRange(stream, 0, 1));
  }
}