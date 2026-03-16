package com.robothy.s3.core.model.request;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.robothy.s3.core.exception.InvalidRangeException;
import org.junit.jupiter.api.Test;

class RangeTest {

  @Test
  void parseAcceptsSupportedForms() {
    assertArrayEquals(new long[] {1, 3}, Range.parse("bytes=1-3").resolve(10));
    assertArrayEquals(new long[] {4, 9}, Range.parse("bytes=4-").resolve(10));
    assertArrayEquals(new long[] {7, 9}, Range.parse("bytes=-3").resolve(10));
  }

  @Test
  void parseUsesFirstRangeWhenMultipleRangesProvided() {
    assertArrayEquals(new long[] {0, 2}, Range.parse("bytes=0-2, 4-6").resolve(10));
  }

  @Test
  void parseRejectsInvalidHeaders() {
    assertThrows(InvalidRangeException.class, () -> Range.parse("0-3"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=3"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-0"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-2-3"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-a"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-1--2"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=-1-2"));
    assertThrows(InvalidRangeException.class, () -> Range.parse("bytes=6-2"));
  }

  @Test
  void resolveClampsEndToObjectSize() {
    assertArrayEquals(new long[] {8, 9}, Range.of(8, 99).resolve(10));
  }

  @Test
  void resolveRejectsUnsatisfiedRanges() {
    assertThrows(InvalidRangeException.class, () -> Range.of(10, 12).resolve(10));
    assertThrows(InvalidRangeException.class, () -> Range.of(1, 2).resolve(0));
  }
}