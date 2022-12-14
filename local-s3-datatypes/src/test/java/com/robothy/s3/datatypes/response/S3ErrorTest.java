package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

class S3ErrorTest {

  @Test
  public void test() throws JsonProcessingException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

    S3Error error = S3Error.builder()
        .code("InternalServerError")
        .message("Internal Server Error")
        .requestId("123")
        .argumentName("Name")
        .argumentValue("Robothy")
        .bucketName("my-bucket")
        .build();

    String xml = xmlMapper.writeValueAsString(error);
    S3Error deserialized = xmlMapper.readValue(xml, S3Error.class);
    assertEquals(error, deserialized);
  }

}