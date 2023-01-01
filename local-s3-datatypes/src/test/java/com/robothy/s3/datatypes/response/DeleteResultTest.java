package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeleteResultTest {

  @Test
  void testSerialization() {
    DeleteResult deleteResult = new DeleteResult();
    deleteResult.setDeletedList(List.of(new S3Error(), new DeleteResult.Deleted()));

    XmlMapper xmlMapper = new XmlMapper();
    Assertions.assertDoesNotThrow(() -> xmlMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(deleteResult));
  }

}