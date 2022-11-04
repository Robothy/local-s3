package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "CompleteMultipartUploadResult")
public class CompleteMultipartUploadResult {

  @JacksonXmlProperty(localName = "Location")
  private String location;

  @JacksonXmlProperty(localName = "Bucket")
  private String bucket;

  @JacksonXmlProperty(localName = "Key")
  private String key;

  @JacksonXmlProperty(localName = "ETag")
  private String etag;

  @JacksonXmlProperty(localName = "ChecksumCRC32")
  private String checksumCRC32;

  @JacksonXmlProperty(localName = "ChecksumCRC32C")
  private String checksumCRC32C;

  @JacksonXmlProperty(localName = "ChecksumSHA1")
  private String checksumSHA1;

  @JacksonXmlProperty(localName = "ChecksumSHA256")
  private String checksumSHA256;

}
