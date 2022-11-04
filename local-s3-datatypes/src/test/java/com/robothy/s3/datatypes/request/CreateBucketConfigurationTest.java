package com.robothy.s3.datatypes.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class CreateBucketConfigurationTest {

  @Test
  @SneakyThrows
  void getLocationConstraint() {
    XmlMapper xmlMapper = new XmlMapper();
    CreateBucketConfiguration createBucketConfiguration = new CreateBucketConfiguration("Loc");
    String xml = xmlMapper.writeValueAsString(createBucketConfiguration);
    CreateBucketConfiguration deserialized = xmlMapper.readValue(xml, CreateBucketConfiguration.class);
    assertEquals(createBucketConfiguration, deserialized);
  }
}