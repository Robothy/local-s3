package com.robothy.s3.core.model.request;

import com.robothy.s3.core.exception.InvalidRangeException;

/**
 * Represents a parsed byte range from an HTTP {@code Range: bytes=...} header (RFC 7233).
 *
 * <p>Three forms are supported:
 * <ul>
 *   <li>{@code bytes=start-end} — {@link #of(long, long)}</li>
 *   <li>{@code bytes=start-}   — {@link #from(long)}</li>
 *   <li>{@code bytes=-suffix}  — {@link #last(long)}</li>
 * </ul>
 */
public class Range {

  private final Long start;

  private final Long end;

  private final Long suffixLength;

  private Range(Long start, Long end, Long suffixLength) {
    this.start = start;
    this.end = end;
    this.suffixLength = suffixLength;
  }

  /** {@code bytes=start-end} */
  public static Range of(long start, long end) {
    return new Range(start, end, null);
  }

  /** {@code bytes=start-} */
  public static Range from(long start) {
    return new Range(start, null, null);
  }

  /** {@code bytes=-suffixLength} */
  public static Range last(long suffixLength) {
    return new Range(null, null, suffixLength);
  }

  /**
   * Parse the value of a {@code Range} header.
   *
   * @throws InvalidRangeException if the header value cannot be parsed
   */
  public static Range parse(String rangeHeader) {
    if (!rangeHeader.startsWith("bytes=")) {
      throw new InvalidRangeException();
    }

    String spec = rangeHeader.substring(6);
    int commaIdx = spec.indexOf(',');
    if (commaIdx >= 0) {
      spec = spec.substring(0, commaIdx).trim();
    }

    int dashIdx = spec.indexOf('-');
    if (dashIdx < 0) {
      throw new InvalidRangeException();
    }

    String startStr = spec.substring(0, dashIdx).trim();
    String endStr = spec.substring(dashIdx + 1).trim();

    try {
      if (startStr.isEmpty()) {
        if (endStr.isEmpty()) {
          throw new InvalidRangeException();
        }
        long suffix = Long.parseLong(endStr);
        if (suffix <= 0) {
          throw new InvalidRangeException();
        }
        return last(suffix);
      }

      long start = Long.parseLong(startStr);
      if (start < 0) {
        throw new InvalidRangeException();
      }

      if (endStr.isEmpty()) {
        return from(start);
      }

      long end = Long.parseLong(endStr);
      if (end < 0 || start > end) {
        throw new InvalidRangeException();
      }
      return of(start, end);
    } catch (NumberFormatException e) {
      throw new InvalidRangeException();
    }
  }

  /**
   * Resolve concrete start/end byte positions given the total object size.
   *
   * @return {@code long[2]} with inclusive {@code [start, end]} positions
   * @throws InvalidRangeException if the range cannot be satisfied for this object size
   */
  public long[] resolve(long objectSize) {
    if (objectSize <= 0) {
      throw new InvalidRangeException();
    }

    if (suffixLength != null) {
      long start = Math.max(0, objectSize - suffixLength);
      return new long[]{start, objectSize - 1};
    }

    long s = start;
    long e = end == null ? objectSize - 1 : end;
    if (s < 0 || s >= objectSize || s > e) {
      throw new InvalidRangeException();
    }
    if (e >= objectSize) {
      e = objectSize - 1;
    }
    return new long[]{s, e};
  }

}
