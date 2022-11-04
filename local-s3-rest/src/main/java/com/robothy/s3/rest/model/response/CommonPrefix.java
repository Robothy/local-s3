package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "CommonPrefixes")
public class CommonPrefix {

  @JacksonXmlProperty(localName = "Prefix")
  private String prefix;

}
