package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "InitiateMultipartUploadResult")
public class InitiateMultipartUploadResult {

  @JacksonXmlProperty(localName = "Bucket")
  private String bucket;

  @JacksonXmlProperty(localName = "Key")
  private String key;

  @JacksonXmlProperty(localName = "UploadId")
  private String uploadId;

}
