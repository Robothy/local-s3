package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListMultipartUploadsResultTest {

  private final XmlMapper xmlMapper = new XmlMapper();

  private static final String TEST_XML = "<ListMultipartUploadsResult><Bucket>my-bucket</Bucket><KeyMarker>key1</KeyMarker><UploadIdMarker>upload1</UploadIdMarker><NextKeyMarker>key2</NextKeyMarker><NextUploadIdMarker>upload2</NextUploadIdMarker><MaxUploads>1000</MaxUploads><IsTruncated>true</IsTruncated><Upload><Key>myfile.txt</Key><UploadId>123</UploadId><StorageClass>STANDARD</StorageClass><Initiated>2023-01-01T00:00:00.000Z</Initiated><Initiator><ID>system</ID></Initiator><Owner><ID>owner123</ID><DisplayName>owner</DisplayName></Owner></Upload><CommonPrefixes><Prefix>prefix1/</Prefix></CommonPrefixes><CommonPrefixes><Prefix>prefix2/</Prefix></CommonPrefixes></ListMultipartUploadsResult>";

  @Test
  void testSerializeDeserialize() throws Exception {
    ListMultipartUploadsResult listMultipartUploadsResult = xmlMapper.readValue(TEST_XML, ListMultipartUploadsResult.class);
    String serialized = xmlMapper.writeValueAsString(listMultipartUploadsResult);
    ListMultipartUploadsResult deserialized = xmlMapper.readValue(serialized, ListMultipartUploadsResult.class);
    assertEquals(listMultipartUploadsResult, deserialized);
  }
}