package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "LocationConstraint")
public class LocationConstraint {

  @JacksonXmlText
  private String locationConstraint;

}
