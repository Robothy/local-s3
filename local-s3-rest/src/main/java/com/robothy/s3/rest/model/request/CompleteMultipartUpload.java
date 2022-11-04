package com.robothy.s3.rest.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "CompleteMultipartUpload")
public class CompleteMultipartUpload {

  @JacksonXmlProperty(localName = "Part")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<CompletedPart> parts;

}
