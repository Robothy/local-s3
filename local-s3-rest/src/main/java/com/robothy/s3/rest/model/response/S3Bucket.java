package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Setter;

@JacksonXmlRootElement(localName = "Bucket")
@Setter
@AllArgsConstructor
public class S3Bucket {

  @JacksonXmlProperty(localName = "Name")
  private String name;

  @JacksonXmlProperty(localName = "CreationDate")
  @JsonSerialize(converter = AmazonInstantConverter.class)
  private Instant creationDate;

}
