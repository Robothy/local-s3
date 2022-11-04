package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.converter.AmazonDateConverter;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@JacksonXmlRootElement(localName = "DeleteMarker")
public class DeleteMarkerEntry implements VersionItem {

  @JacksonXmlProperty(localName = "IsLatest")
  protected boolean latest;

  @Setter
  @JacksonXmlProperty(localName = "Key")
  protected String key;

  @JacksonXmlProperty(localName = "LastModified")
  @JsonSerialize(converter = AmazonDateConverter.class)
  protected Date lastModified;

  @JacksonXmlProperty(localName = "Owner")
  protected Owner owner;

  @JacksonXmlProperty(localName = "VersionId")
  protected String versionId;

}
