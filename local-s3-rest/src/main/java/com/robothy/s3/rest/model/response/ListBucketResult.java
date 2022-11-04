package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.response.Object;
import java.util.List;
import lombok.Builder;

@Builder
@JacksonXmlRootElement(localName = "ListBucketResult")
public class ListBucketResult {

  @JacksonXmlProperty(localName = "IsTruncated")
  private boolean isTruncated;

  @JacksonXmlProperty(localName = "Marker")
  private String marker;

  @JacksonXmlProperty(localName = "NextMarker")
  private String nextMarker;

  @JacksonXmlProperty(localName = "Name")
  private String name;

  @JacksonXmlProperty(localName = "Prefix")
  private String prefix;

  @JacksonXmlProperty(localName = "Delimiter")
  private Character delimiter;

  @JacksonXmlProperty(localName = "MaxKeys")
  private int maxKeys;

  @JacksonXmlProperty(localName = "EncodingType")
  private String encodingType;

  @JacksonXmlProperty(localName = "Contents")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Object> contents;

  @JacksonXmlProperty(localName = "CommonPrefixes")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<CommonPrefix> commonPrefixes;

}
