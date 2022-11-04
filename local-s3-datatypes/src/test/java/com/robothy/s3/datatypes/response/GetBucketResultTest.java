package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
        .creationDate(new Date())
        .publicAccessBlockEnabled(false)
        .build();
    String xml = xmlMapper.writeValueAsString(getBucketResult);
    GetBucketResult deserialized = xmlMapper.readValue(xml, GetBucketResult.class);
    assertEquals(getBucketResult, deserialized);
  }

}