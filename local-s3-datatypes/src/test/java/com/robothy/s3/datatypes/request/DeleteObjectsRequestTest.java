package com.robothy.s3.datatypes.request;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.s3.datatypes.ObjectIdentifier;
import org.junit.jupiter.api.Test;

class DeleteObjectsRequestTest {

  @Test
  void testDeserializeDeleteObjectsRequest() throws JsonProcessingException {

    String xml = "<Delete>\n" +
        "  <Object>\n" +
        "    <Key>a.txt</Key>\n" +
        "    <VersionId>null</VersionId>\n" +
        "  </Object>\n" +
        "  <Object>\n" +
        "    <Key>b.txt</Key>\n" +
        "  </Object>\n" +
        "  <Quiet>true</Quiet>\n" +
        "</Delete>";

    XmlMapper xmlMapper = new XmlMapper();
    DeleteObjectsRequest request = xmlMapper.readValue(xml, DeleteObjectsRequest.class);
    assertEquals(2, request.getObjects().size());
    ObjectIdentifier objectIdentifier1 = request.getObjects().get(0);
    assertEquals("a.txt", objectIdentifier1.getKey());
    assertTrue(objectIdentifier1.getVersionId().isPresent());
    assertEquals("null", objectIdentifier1.getVersionId().get());

    ObjectIdentifier objectIdentifier2 = request.getObjects().get(1);
    assertEquals("b.txt", objectIdentifier2.getKey());
    assertTrue(objectIdentifier2.getVersionId().isEmpty());

    assertTrue(request.isQuiet());
  }

}