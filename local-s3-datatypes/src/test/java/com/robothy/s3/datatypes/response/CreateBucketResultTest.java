package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class CreateBucketResultTest {

  @Test
  void test() throws IOException {
    XmlMapper mapper = new XmlMapper();
    ObjectWriter writer = mapper.writer();
    ObjectReader reader = mapper.reader();

    CreateBucketResult createBucketResult = CreateBucketResult.builder()
        .bucketArn("abc")
        .build();
    mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

    String serialized = writer.with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN).writeValueAsString(createBucketResult);
    CreateBucketResult deserialized = reader.readValue(serialized, CreateBucketResult.class);
    assertEquals(createBucketResult, deserialized);
  }

}