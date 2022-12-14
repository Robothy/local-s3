package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.Date;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class GetBucketResultTest {

  @SneakyThrows
  @Test
  public void test() {

    XmlMapper xmlMapper = new XmlMapper();
    GetBucketResult getBucketResult = GetBucketResult.builder()
        .bucket("test")
        .creationDate(Instant.EPOCH)
        .publicAccessBlockEnabled(false)
        .build();
    String xml = xmlMapper.writeValueAsString(getBucketResult);
    xmlMapper.registerModule(new JavaTimeModule());
    GetBucketResult deserialized = xmlMapper.readValue(xml, GetBucketResult.class);
    assertEquals(getBucketResult, deserialized);
  }

}