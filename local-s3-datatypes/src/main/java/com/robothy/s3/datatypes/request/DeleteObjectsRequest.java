package com.robothy.s3.datatypes.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.ObjectIdentifier;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Delete")
public class DeleteObjectsRequest {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "Object")
  private List<ObjectIdentifier> objects = new ArrayList<>();

  @JacksonXmlProperty(localName = "Quiet")
  private boolean quiet;

}
