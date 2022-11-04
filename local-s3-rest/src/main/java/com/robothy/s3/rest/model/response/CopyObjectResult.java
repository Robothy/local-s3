package com.robothy.s3.rest.model.response;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.converter.AmazonDateConverter;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "CopyObjectResult")
public class CopyObjectResult {

  @JacksonXmlProperty(localName = "LastModified")
  @JsonSerialize(converter = AmazonDateConverter.class)
  private Date lastModified;

  @JacksonXmlProperty(localName = "ETag")
  private String etag;

}
