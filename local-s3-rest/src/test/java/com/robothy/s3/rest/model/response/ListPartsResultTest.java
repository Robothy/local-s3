package com.robothy.s3.rest.model.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.enums.StorageClass;
import com.robothy.s3.rest.utils.XmlUtils;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListPartsResultTest {

  @Test
  void testSerialization() throws Exception {

    ListPartsResult.Part part1 = ListPartsResult.Part.builder()
        .partNumber(1)
        .lastModified(Instant.now())
        .size(12)
        .etag("etag")
        .build();

    ListPartsResult.Part part2 = ListPartsResult.Part.builder()
        .partNumber(2)
        .lastModified(Instant.now())
        .size(12)
        .etag("etag")
        .build();

    ListPartsResult listPartsResult = ListPartsResult.builder()
        .bucket("bucket")
        .key("key")
        .uploadId("uploadId")
        .isTruncated(true)
        .maxParts(100)
        .nextPartNumberMarker(1)
        .partNumberMarker(0)
        .initiator(Owner.DEFAULT_OWNER)
        .owner(Owner.DEFAULT_OWNER)
        .storageClass(StorageClass.STANDARD)
        .parts(List.of(part1, part2))
        .build();

    String xml = XmlUtils.toPrettyXml(listPartsResult);
    ListPartsResult deserialized = XmlUtils.fromXml(xml, ListPartsResult.class);
    assertEquals(listPartsResult, deserialized);
    //System.out.println(xml);
  }

}