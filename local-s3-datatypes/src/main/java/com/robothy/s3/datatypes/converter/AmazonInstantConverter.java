package com.robothy.s3.datatypes.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.util.Date;

/**
 * Convert to Amazon instant.
 */
public class AmazonInstantConverter extends StdConverter<Instant, String> {

  @Override
  public String convert(Instant value) {
    return value.toString();
  }

}
