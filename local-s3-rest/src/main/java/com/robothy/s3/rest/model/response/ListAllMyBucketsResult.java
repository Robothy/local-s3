package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.Owner;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JacksonXmlRootElement(localName = "ListAllMyBucketsResult")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ListAllMyBucketsResult {

  @JacksonXmlElementWrapper(localName = "Buckets")
  @JacksonXmlProperty(localName = "Bucket")
  private List<S3Bucket> buckets;

  @JacksonXmlProperty(localName = "Owner")
  private Owner owner;

}
