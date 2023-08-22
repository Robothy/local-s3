package com.robothy.s3.rest.utils;

import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.constants.AmzHeaderValues;
import com.robothy.s3.rest.model.request.DecodedAmzRequestBody;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.InputStream;
import java.util.Optional;

/**
 * HTTP Request related utils.
 */
public class RequestUtils {

  /**
   * Get request body as decoded input stream.
   *
   * @param request HTTP request.
   * @return decoded input stream.
   */
  @Deprecated
  public static InputStream getInputStream(HttpRequest request) {
    InputStream inputStream = new ByteBufInputStream(request.getBody());
    if (request.header(AmzHeaderNames.X_AMZ_CONTENT_SHA256)
        .map(AmzHeaderValues.STREAMING_AWS4_HMAC_SHA_256_PAYLOAD::equals).orElse(false)) {
      return InputStreamUtils.decodeAwsChunkedEncodingInputStream(inputStream);
    } else {
      return inputStream;
    }
  }

  /**
   * Get the decoded request body. Decode the request body if needed.
   *
   * @param request HTTP request.
   * @return decoded request body.
   */
  public static DecodedAmzRequestBody getBody(HttpRequest request) {
    DecodedAmzRequestBody result = new DecodedAmzRequestBody();
    if (request.header(AmzHeaderNames.X_AMZ_CONTENT_SHA256)
        .map(AmzHeaderValues.STREAMING_AWS4_HMAC_SHA_256_PAYLOAD::equals).orElse(false)) {
      result.setDecodedBody(new AwsChunkedDecodingInputStream(new ByteBufInputStream(request.getBody())));
      result.setDecodedContentLength(request.header(AmzHeaderNames.X_AMZ_DECODED_CONTENT_LENGTH).map(Long::parseLong)
          .orElseThrow(() -> new IllegalArgumentException(AmzHeaderNames.X_AMZ_DECODED_CONTENT_LENGTH + "header not exist.")));
    } else {
      result.setDecodedBody(new ByteBufInputStream(request.getBody()));
      result.setDecodedContentLength(request.header(HttpHeaderNames.CONTENT_LENGTH.toString()).map(Long::parseLong)
          .orElseThrow(() -> new IllegalArgumentException("Content-Type is required.")));
    }
    return result;
  }

  public static Optional<String> getETag(HttpRequest request) {
    return request.header(HttpHeaderNames.ETAG.toString());
  }

}
