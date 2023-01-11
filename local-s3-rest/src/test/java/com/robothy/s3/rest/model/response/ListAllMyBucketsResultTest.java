package com.robothy.s3.rest.model.response;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.s3.datatypes.Owner;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListAllMyBucketsResultTest {

  @Test
  void testSerialization() throws JsonProcessingException {
    ListAllMyBucketsResult listAllMyBucketsResult = new ListAllMyBucketsResult();
    listAllMyBucketsResult.setBuckets(List.of(new S3Bucket("bucket1", Instant.now()),
        new S3Bucket("bucket2", Instant.now())));
    listAllMyBucketsResult.setOwner(new Owner("LocalS3", "001"));
    XmlMapper xmlMapper = new XmlMapper();
    assertDoesNotThrow(() -> xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(listAllMyBucketsResult));
    //System.out.println(xml);
  }

}