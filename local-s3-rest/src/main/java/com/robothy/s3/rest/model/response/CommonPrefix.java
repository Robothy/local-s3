package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "CommonPrefixes")
@EqualsAndHashCode
public class CommonPrefix {

  @JacksonXmlProperty(localName = "Prefix")
  private String prefix;

}
