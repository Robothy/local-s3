package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "LocationConstraint")
public class LocationConstraint {

  @JacksonXmlProperty(localName = "LocationConstraint")
  private String locationConstraint;

}
