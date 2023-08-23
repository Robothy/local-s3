package com.robothy.s3.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  /**
   * Encode the given string to url format except slash.
   */
  public static String urlEncodeEscapeSlash(String str) {
    if (str == null) {
      return null;
    }
    String[] ss = str.split("/");
    String encoded = Stream.of(ss)
        .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
        .collect(Collectors.joining("/"));
    return str.endsWith("/") ? encoded + "/" : encoded;
  }

}
