package com.robothy.s3.datatypes.response;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

class LocationConstraintTest {

  @Test
  void serialization() throws Exception {
    XmlMapper xmlMapper = new XmlMapper();
    LocationConstraint locationConstraint = LocationConstraint.builder()
        .locationConstraint("local")
        .build();
    assertEquals("<LocationConstraint><LocationConstraint>local</LocationConstraint></LocationConstraint>", xmlMapper.writeValueAsString(locationConstraint));
  }

}