package com.robothy.s3.rest.utils;

import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.constants.AmzHeaderValues;
import com.robothy.s3.rest.model.request.DecodedAmzRequestBody;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

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

  /**
   * Extract tagging from the HTTP header.
   *
   * @param request HTTP request.
   * @return tagging.
   */
  public static Optional<String[][]> extractTagging(HttpRequest request) {
    Optional<String> taggingOpt = request.header(AmzHeaderNames.X_AMZ_TAGGING);
    String tagging;
    if (taggingOpt.isEmpty() || StringUtils.isBlank(tagging = taggingOpt.get())) {
      return Optional.empty();
    }

    String[] tags = tagging.split("&");
    String[][] tagSet = new String[tags.length][2];
    for (int i = 0; i < tags.length; i++) {
      String[] kv = tags[i].split("=");
      if (kv.length != 2) {
        throw new LocalS3InvalidArgumentException(AmzHeaderNames.X_AMZ_TAGGING, "Invalid tagging format.");
      }

      tagSet[i][0] = kv[0];
      tagSet[i][1] = kv[1];
    }

    return Optional.of(tagSet);
  }


  /**
   * Extract user metadata from headers. User metadata in headers that start with {@linkplain AmzHeaderNames#X_AMZ_META_PREFIX}.
   *
   * <p><a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html#UserMetadata">User-defined object metadata</a>
   *
   * @param request HTTP request
   * @return fetched user metadata.
   */
  public static Map<String, String> extractUserMetadata(HttpRequest request) {
    Map<String, String> userMetadata = new HashMap<>();
    request.getHeaders()
      .forEach((k, v) -> {
        if (k.toString().startsWith(AmzHeaderNames.X_AMZ_META_PREFIX)) {
          String metaName = RequestAssertions.assertUserMetadataHeaderIsValid(k.toString());
          userMetadata.put(metaName, v);
        }
      });
    return userMetadata;
  }

}
