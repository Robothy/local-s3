package com.robothy.s3.rest.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class CompletedPart {

  @JacksonXmlProperty(localName = "ChecksumCRC32")
  private String checksumCRC32;

  @JacksonXmlProperty(localName = "ChecksumCRC32C")
  private String checksumCRC32C;

  @JacksonXmlProperty(localName = "ChecksumSHA1")
  private String checksumSHA1;

  @JacksonXmlProperty(localName = "ChecksumSH1256")
  private String checksumSHA256;

  @JacksonXmlProperty(localName = "ETag")
  private String etag;

  @JacksonXmlProperty(localName = "PartNumber")
  private int partNumber;

}
