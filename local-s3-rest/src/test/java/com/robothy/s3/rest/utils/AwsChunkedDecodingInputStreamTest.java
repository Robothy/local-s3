package com.robothy.s3.rest.utils;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class AwsChunkedDecodingInputStreamTest {

  @Test
  void testSimpleChunk() throws IOException {
    // Single chunk followed by 0 chunk
    String chunkedData = "3;chunk-signature=abcd\r\nabc\r\n0;chunk-signature=zzzz\r\n";
    try (AwsChunkedDecodingInputStream in =
             new AwsChunkedDecodingInputStream(
                 new ByteArrayInputStream(chunkedData.getBytes()))) {
      StringBuilder sb = new StringBuilder();
      int c;
      while ((c = in.read()) != -1) {
        sb.append((char) c);
      }
      assertEquals("abc", sb.toString());
    }
  }

  @Test
  void testMultipleChunks() throws IOException {
    // Two chunks, then 0 chunk
    String chunkedData = "3;chunk-signature=abcd\r\nabc\r\n2;chunk-signature=efgh\r\nxy\r\n0;chunk-signature=zzzz\r\n";
    try (AwsChunkedDecodingInputStream in =
             new AwsChunkedDecodingInputStream(
                 new ByteArrayInputStream(chunkedData.getBytes()))) {
      StringBuilder sb = new StringBuilder();
      int c;
      while ((c = in.read()) != -1) {
        sb.append((char) c);
      }
      assertEquals("abcxy", sb.toString());
    }
  }

  @Test
  void testZeroChunk() throws IOException {
    // Immediate zero chunk
    String chunkedData = "0;chunk-signature=abcd\r\n";
    try (AwsChunkedDecodingInputStream in =
             new AwsChunkedDecodingInputStream(
                 new ByteArrayInputStream(chunkedData.getBytes()))) {
      assertEquals(-1, in.read());
    }
  }

  @Test
  void testNoTrailingCrLf() throws IOException {
    // Missing final CRLF after size line
    String chunkedData = "3;chunk-signature=abcd\r\nabc\r\n0;chunk-signature=zzzz";
    try (AwsChunkedDecodingInputStream in =
             new AwsChunkedDecodingInputStream(
                 new ByteArrayInputStream(chunkedData.getBytes()))) {
      StringBuilder sb = new StringBuilder();
      int c;
      while ((c = in.read()) != -1) {
        sb.append((char) c);
      }
      assertEquals("abc", sb.toString());
    }
  }

}
