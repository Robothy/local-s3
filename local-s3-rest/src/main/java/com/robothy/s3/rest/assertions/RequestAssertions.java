package com.robothy.s3.rest.assertions;


import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import java.util.List;
import java.util.Optional;

/**
 * HTTP requests related assertions.
 */
public class RequestAssertions {

  /**
   * Assert that the request provided the bucket name in path variable.
   *
   * @param request HTTP request.
   * @return the bucket name.
   */
  public static String assertBucketNameProvided(HttpRequest request) {
    return request.parameter("bucket")
        .orElseThrow(() -> new IllegalArgumentException("Bucket name must be provided in request path. " +
            "You may need to make the Amazon S3 client using path style via 'AmazonS3ClientBuilder#withPathStyleAccessEnabled(true)' " +
            "or ClientConfiguration()#withDisableHostPrefixInjection(true)."));
  }

  /**
   * Assert that the object key in provided in the request.
   *
   * @param request HTTP request.
   * @return the object key.
   */
  public static String assertObjectKeyProvided(HttpRequest request) {
    return request.parameter("key")
        .map(key -> key.startsWith("/") ? key.substring(1) : key)
        .orElseThrow(() -> new IllegalArgumentException("The object key is required."));
  }

  /**
   * Assert that user provided delimiter is a character.
   *
   * @param request HTTP request.
   * @return fetched character or null.
   */
  public static Optional<String> assertDelimiterIsValid(HttpRequest request) {
    return request.parameter("delimiter");
  }

  /**
   * Assert the value of encoding-type is "url".
   *
   * @param request HTTP request.
   * @return fetched encoding type or null.
   */
  public static Optional<String> assertEncodingTypeIsValid(HttpRequest request) {
    return request.parameter("encoding-type").map(encodingType -> {
      if (!"url".equalsIgnoreCase(encodingType)) {
        throw new LocalS3InvalidArgumentException("encoding-type", encodingType, "Invalid Encoding Method specified in Request");
      }
      return encodingType;
    });
  }

  /**
   * Assert the provided part number is valid. Between 1~10000.
   *
   * @param request HTTP request.
   * @return the fetched part number.
   */
  public static int assertPartNumberIsValid(HttpRequest request) {
    String partNumber = request.parameter("partNumber").orElseThrow(
        () -> new IllegalArgumentException("'partNumber' is required."));
    int number = Integer.parseInt(partNumber);
    if (number < 1 || number > 10000) {
      throw new IllegalArgumentException("The 'partNumber' must be a positive integer between 1 and 10000");
    }
    return number;
  }

  /**
   * Assert that the uploadId is in the query parameters.
   *
   * @param request HTTP request.
   * @return fetched upload ID.
   */
  public static String assertUploadIdIsProvided(HttpRequest request) {
    return request.parameter("uploadId").orElseThrow(
        () -> new IllegalArgumentException("'uploadId' is required."));
  }

  /**
   * Assert that the user-defined metadata header is valid.
   *
   * @param userMetaHeaderName the user-defined object metadata header name.
   * @return the user-defined object metadata name without "x-amz-meta-" prefix.
   */
  public static String assertUserMetadataHeaderIsValid(String userMetaHeaderName) {
    if (!userMetaHeaderName.startsWith(AmzHeaderNames.X_AMZ_META_PREFIX)
        || userMetaHeaderName.length() == AmzHeaderNames.X_AMZ_META_PREFIX.length()) {
      throw new IllegalArgumentException("Invalid user-defined object metadata key: " + userMetaHeaderName + ".");
    }
    return userMetaHeaderName.substring(AmzHeaderNames.X_AMZ_META_PREFIX.length()).toLowerCase();
  }

  /**
   * Assert that the HTTP request doesn't have the parameter, or the parameter is integer.
   */
  public static Integer assertIntegerParameterOrNull(HttpRequest request, String queryParam) {
    List<String> values = request.getParams().get(queryParam);
    if (null == values || values.isEmpty()) {
      return null;
    }

    try {
      return Integer.parseInt(values.get(0));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value of " + queryParam + " must be an integer.");
    }
  }
}
