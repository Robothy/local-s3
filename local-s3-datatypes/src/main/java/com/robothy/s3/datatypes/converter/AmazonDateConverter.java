package com.robothy.s3.datatypes.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.Date;

public class AmazonDateConverter extends StdConverter<Date, String> {

  @Override
  public String convert(Date value) {
    return value.toInstant().toString();
  }

}
