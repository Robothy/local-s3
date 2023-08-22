package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import com.robothy.s3.datatypes.enums.StorageClass;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JacksonXmlRootElement(localName = "ListPartsResult")
public class ListPartsResult {

  @JacksonXmlProperty(localName = "Bucket")
  private String bucket;

  @JacksonXmlProperty(localName = "Key")
  private String key;

  @JacksonXmlProperty(localName = "UploadId")
  private String uploadId;

  @JacksonXmlProperty(localName = "PartNumberMarker")
  private int partNumberMarker;

  @JacksonXmlProperty(localName = "NextPartNumberMarker")
  private int nextPartNumberMarker;

  @JacksonXmlProperty(localName = "MaxParts")
  private int maxParts;

  @JacksonXmlProperty(localName = "IsTruncated")
  private boolean isTruncated;

  @JacksonXmlProperty(localName = "Part")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Part> parts;

  @JacksonXmlProperty(localName = "Initiator")
  private Owner initiator;

  @JacksonXmlProperty(localName = "Owner")
  private Owner owner;

  @JacksonXmlProperty(localName = "StorageClass")
  private StorageClass storageClass;

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Part {

    @JacksonXmlProperty(localName = "ETag")
    private String etag;

    @JacksonXmlProperty(localName = "LastModified")
    @JsonSerialize(converter = AmazonInstantConverter.class)
    private Instant lastModified;

    @JacksonXmlProperty(localName = "PartNumber")
    private int partNumber;

    @JacksonXmlProperty(localName = "Size")
    private long size;

  }

}
