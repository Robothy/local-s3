package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

class ErrorTest {

  @Test
  public void test() throws JsonProcessingException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

    Error error = Error.builder()
        .code("InternalServerError")
        .message("Internal Server Error")
        .requestId("123")
        .argumentName("Name")
        .argumentValue("Robothy")
        .build();

    String xml = xmlMapper.writeValueAsString(error);
    Error deserialized = xmlMapper.readValue(xml, Error.class);
    assertEquals(error, deserialized);
  }

}