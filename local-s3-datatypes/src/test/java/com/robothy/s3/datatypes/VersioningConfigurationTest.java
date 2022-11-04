package com.robothy.s3.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class VersioningConfigurationTest {

  @Test
  public void test() throws IOException {
    XmlMapper mapper = new XmlMapper();
    ObjectWriter writer = mapper.writer();
    ObjectReader reader = mapper.reader();

    VersioningConfiguration versioningConfiguration = VersioningConfiguration.builder()
        .status(VersioningConfiguration.Enabled)
        .build();

    String xmlStr = writer.writeValueAsString(versioningConfiguration);
    VersioningConfiguration deserialized =
        reader.readValue(xmlStr, VersioningConfiguration.class);
    assertEquals(VersioningConfiguration.Enabled, deserialized.getStatus());
  }

}