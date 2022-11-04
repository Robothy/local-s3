package com.robothy.s3.rest.utils;

import java.io.InputStream;

/**
 * {@linkplain InputStream} utils.
 */
public class InputStreamUtils {

  /**
   * Decode the Aws chunked encoding input stream.
   *
   * @param encoded the checked encoding input stream.
   * @return Decoded input stream.
   */
  public static InputStream decodeAwsChunkedEncodingInputStream(InputStream encoded) {
    return new AwsChunkedDecodingInputStream(encoded);
  }

}
