package com.robothy.s3.rest.model.response;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "CopyObjectResult")
public class CopyObjectResult {

  @JacksonXmlProperty(localName = "LastModified")
  @JsonSerialize(converter = AmazonInstantConverter.class)
  private Instant lastModified;

  @JacksonXmlProperty(localName = "ETag")
  private String etag;

}
