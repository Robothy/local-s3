package com.robothy.s3.rest.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class AwsUnsignedChunkedDecodingInputStreamTest {

  @Test
  void testSimpleDecoding() throws IOException {
    String chunkedData = "3\r\nabc\r\n1\r\nz\r\n0\r\n\r\n";
    try (AwsUnsignedChunkedDecodingInputStream in =
             new AwsUnsignedChunkedDecodingInputStream(
                 new ByteArrayInputStream(chunkedData.getBytes()))) {
      StringBuilder sb = new StringBuilder();
      int c;
      while ((c = in.read()) != -1) {
        sb.append((char) c);
      }
      assertEquals("abcz", sb.toString());
    }
  }

  @Test
  void testEmptyFile() throws IOException {
    try (AwsUnsignedChunkedDecodingInputStream in =
            new AwsUnsignedChunkedDecodingInputStream(new ByteArrayInputStream(new byte[0]))) {
        assertEquals(-1, in.read());
    }
  }

  @Test
  void testUnexpectedEndOfChunkSizeLine() throws IOException {
    String chunkedData = "\r\n3\r\nabc\r\n0\r\n\r\n";
    // The first line is empty, second line is valid chunk size
    try (AwsUnsignedChunkedDecodingInputStream in =
            new AwsUnsignedChunkedDecodingInputStream(new ByteArrayInputStream(chunkedData.getBytes()))) {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
        }
        assertEquals("abc", sb.toString());
    }
  }

  @Test
  void testNoTrailingCrLf() throws IOException {
    // This chunked sequence lacks trailing CRLF
    String chunkedData = "3\r\nabc\r\n1\r\nz\r\n0\r\n";
    try (AwsUnsignedChunkedDecodingInputStream in =
            new AwsUnsignedChunkedDecodingInputStream(new ByteArrayInputStream(chunkedData.getBytes()))) {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
        }
        assertEquals("abcz", sb.toString());
    }
  }

}
