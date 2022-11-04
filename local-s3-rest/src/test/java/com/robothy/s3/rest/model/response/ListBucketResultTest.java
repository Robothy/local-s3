package com.robothy.s3.rest.model.response;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.s3.datatypes.response.Object;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListBucketResultTest {

  @Test
  void serialization() throws JsonProcessingException {
    ListBucketResult listBucketResult = ListBucketResult.builder().isTruncated(false)
        .delimiter('/')
        .maxKeys(100)
        .encodingType("url")
        .prefix("dir")
        .contents(List.of(new Object(), new Object()))
        .commonPrefixes(List.of(new CommonPrefix("a/"), new CommonPrefix("b/")))
        .build();

    new XmlMapper().writerWithDefaultPrettyPrinter()
        .writeValueAsString(listBucketResult);
  }

}