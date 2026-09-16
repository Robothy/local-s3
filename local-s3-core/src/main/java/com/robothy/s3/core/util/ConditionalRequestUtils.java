package com.robothy.s3.core.util;

import com.robothy.s3.core.exception.PreconditionFailedException;
import java.util.Objects;

/**
 * Utility methods for S3 conditional requests, e.g. {@code If-Match} and
 * {@code If-None-Match} headers.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">PutObject</a>
 */
public class ConditionalRequestUtils {

  /**
   * Check whether the conditional request headers hold. If not, a
   * {@linkplain PreconditionFailedException} is thrown.
   *
   * <p>Semantics:
   * <ul>
   *   <li>{@code If-Match}: fails when the object does not exist or when the current ETag
   *       does not match. The value {@code *} only requires the object to exist.</li>
   *   <li>{@code If-None-Match}: fails when the object exists and the current ETag matches.
   *       The value {@code *} fails whenever the object exists.</li>
   *   <li>When both headers are present, both conditions must hold.</li>
   * </ul>
   *
   * @param ifMatch     value of the {@code If-Match} header, may be null.
   * @param ifNoneMatch value of the {@code If-None-Match} header, may be null.
   * @param currentEtag the current ETag of the object without surrounding quotes, may be null.
   * @param exists      whether the object currently exists.
   */
  public static void assertConditionalRequest(String ifMatch, String ifNoneMatch,
                                              String currentEtag, boolean exists) {
    if (Objects.nonNull(ifMatch)) {
      if (!exists) {
        throw new PreconditionFailedException("At least one of the preconditions you specified did not hold.");
      }
      if (!isWildcard(ifMatch) && !etagMatches(ifMatch, currentEtag)) {
        throw new PreconditionFailedException("At least one of the preconditions you specified did not hold.");
      }
    }

    if (Objects.nonNull(ifNoneMatch) && exists) {
      if (isWildcard(ifNoneMatch) || etagMatches(ifNoneMatch, currentEtag)) {
        throw new PreconditionFailedException("At least one of the preconditions you specified did not hold.");
      }
    }
  }

  private static boolean isWildcard(String conditionalEtag) {
    return "*".equals(conditionalEtag.trim());
  }

  /**
   * Compare the ETag values ignoring surrounding double quotes. Both the header value and the
   * stored ETag may or may not be quoted when they come from different clients.
   */
  private static boolean etagMatches(String conditionalEtag, String currentEtag) {
    if (Objects.isNull(currentEtag)) {
      return false;
    }
    String normalizedCurrent = stripQuotes(currentEtag.trim());
    String[] candidates = conditionalEtag.split(",");
    for (String candidate : candidates) {
      if (normalizedCurrent.equals(stripQuotes(candidate.trim()))) {
        return true;
      }
    }
    return false;
  }

  private static String stripQuotes(String etag) {
    if (etag.length() >= 2 && etag.charAt(0) == '"' && etag.charAt(etag.length() - 1) == '"') {
      return etag.substring(1, etag.length() - 1);
    }
    return etag;
  }

}
