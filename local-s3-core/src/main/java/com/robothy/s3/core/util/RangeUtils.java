package com.robothy.s3.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for applying a resolved byte range to an {@link InputStream}.
 */
public class RangeUtils {

  /**
   * Skip {@code start} bytes in {@code stream} and return a new {@link InputStream} containing
   * exactly {@code length} bytes from that position. The original stream is closed.
   */
  public static InputStream applyRange(InputStream stream, long start, long length) {
    try {
      long remaining = start;
      while (remaining > 0) {
        long skipped = stream.skip(remaining);
        if (skipped <= 0) {
          break;
        }
        remaining -= skipped;
      }
      byte[] data = stream.readNBytes((int) length);
      stream.close();
      return new ByteArrayInputStream(data);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
