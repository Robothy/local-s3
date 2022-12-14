package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import com.robothy.s3.datatypes.enums.CheckSumAlgorithm;
import com.robothy.s3.datatypes.enums.StorageClass;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@JacksonXmlRootElement(localName = "Version")
public class ObjectVersion implements VersionItem {

  @JacksonXmlProperty(localName = "IsLatest")
  protected boolean latest;

  @Setter
  @JacksonXmlProperty(localName = "Key")
  protected String key;

  @JsonSerialize(converter = AmazonInstantConverter.class)
  @JacksonXmlProperty(localName = "LastModified")
  protected Instant lastModified;

  @JacksonXmlProperty(localName = "Owner")
  protected Owner owner;

  @JacksonXmlProperty(localName = "VersionId")
  protected String versionId;

  @JacksonXmlProperty(localName = "CheckSumAlgorithm")
  private CheckSumAlgorithm checkSumAlgorithm;

  @JacksonXmlProperty(localName = "ETag")
  private String etag;

  @JacksonXmlProperty(localName = "Size")
  private long size;

  @JacksonXmlProperty(localName = "StorageClass")
  private StorageClass storageClass;

}
