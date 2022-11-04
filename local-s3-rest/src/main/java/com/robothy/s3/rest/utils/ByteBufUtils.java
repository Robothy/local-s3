package com.robothy.s3.rest.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@linkplain ByteBuf} related utils.
 */
public class ByteBufUtils {

  /**
   * Transfer all data from an {@linkplain InputStream} to a {@linkplain ByteBuf}.
   * The {@code  inputStream} will be closed.
   *
   * @param inputStream input stream to read.
   * @return a {@linkplain ByteBuf} with data from the {@code inputStream}.
   */
  public static ByteBuf fromInputStream(InputStream inputStream) {
    ByteBuf buffer = Unpooled.buffer();
    try (inputStream) {
      byte[] buf = new byte[8192];
      int len;
      while ((len = inputStream.read(buf)) != -1) {
        buffer.writeBytes(buf, 0, len);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return buffer;
  }

}
