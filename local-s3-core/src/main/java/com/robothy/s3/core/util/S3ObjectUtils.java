package com.robothy.s3.core.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.digest.DigestUtils;

public class S3ObjectUtils {

  /**
   * Calculate the etag of the given input stream.
   */
  public static String etag(InputStream inputStream) {
    try {
      return DigestUtils.md5Hex(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
