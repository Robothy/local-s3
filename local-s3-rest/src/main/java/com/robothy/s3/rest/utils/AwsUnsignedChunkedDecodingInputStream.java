package com.robothy.s3.rest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A counterpart to AwsUnsignedChunkedEncodingInputStream that decodes
 * chunked data without expecting any signature in the chunk extension.
 */
public class AwsUnsignedChunkedDecodingInputStream extends InputStream {
  private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);
  private final InputStream source;
  private int remainingInChunk;

  public AwsUnsignedChunkedDecodingInputStream(InputStream source) {
    this.source = source;
  }

  @Override
  public int read() throws IOException {
    if (remainingInChunk == 0) {
      String chunkSizeHex = readLine();
      while (chunkSizeHex != null && chunkSizeHex.trim().isEmpty()) {
        chunkSizeHex = readLine();
      }
      if (chunkSizeHex == null) {
        return -1;
      }
      remainingInChunk = Integer.parseInt(chunkSizeHex.trim(), 16);
      if (remainingInChunk == 0) {
        // Consume trailing CRLF
        readLine();
        return -1;
      }
    }
    remainingInChunk--;
    return source.read();
  }

  private String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    int prev = -1;
    int cur;
    while ((cur = source.read()) != -1) {
      if (prev == '\r' && cur == '\n') {
        sb.setLength(sb.length() - 1);
        break;
      }
      sb.append((char) cur);
      prev = cur;
    }
    if (sb.length() == 0 && cur == -1) {
      return null;
    }
    return sb.toString();
  }

  @Override
  public void close() throws IOException {
    source.close();
  }
}
