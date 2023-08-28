package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.response.S3Object;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
  private String delimiter;

  @JacksonXmlProperty(localName = "MaxKeys")
  private int maxKeys;

  @JacksonXmlProperty(localName = "EncodingType")
  private String encodingType;

  @JacksonXmlProperty(localName = "Contents")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<S3Object> contents;

  @JacksonXmlProperty(localName = "CommonPrefixes")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<CommonPrefix> commonPrefixes;

}
