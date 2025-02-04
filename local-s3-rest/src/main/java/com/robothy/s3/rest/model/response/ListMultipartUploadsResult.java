package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.Owner;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "ListMultipartUploadsResult")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ListMultipartUploadsResult {

  @JacksonXmlProperty(localName = "Bucket")
  private String bucket;

  @JacksonXmlProperty(localName = "KeyMarker")
  private String keyMarker;

  @JacksonXmlProperty(localName = "UploadIdMarker")
  private String uploadIdMarker;

  @JacksonXmlProperty(localName = "NextKeyMarker")
  private String nextKeyMarker;

  @JacksonXmlProperty(localName = "NextUploadIdMarker")
  private String nextUploadIdMarker;

  @JacksonXmlProperty(localName = "MaxUploads")
  private int maxUploads;

  @JacksonXmlProperty(localName = "IsTruncated")
  private boolean isTruncated;

  @JacksonXmlProperty(localName = "Delimiter")
  private String delimiter;

  @JacksonXmlProperty(localName = "Upload")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Upload> uploads;

  @JacksonXmlProperty(localName = "CommonPrefixes")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<CommonPrefix> commonPrefixes;

  @Builder
  @Getter // Add this annotation
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JacksonXmlRootElement(localName = "Upload")
  @AllArgsConstructor
  @NoArgsConstructor
  @EqualsAndHashCode
  public static class Upload {

    @JacksonXmlProperty(localName = "Key")
    private String key;

    @JacksonXmlProperty(localName = "UploadId")
    private String uploadId;

    @JacksonXmlProperty(localName = "Initiator")
    private Initiator initiator;

    @JacksonXmlProperty(localName = "Owner")
    private Owner owner;

    @JacksonXmlProperty(localName = "StorageClass")
    private String storageClass;

    @JacksonXmlProperty(localName = "Initiated")
    private String initiated;

  }

  @Builder
  @Getter // Add this annotation
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JacksonXmlRootElement(localName = "Initiator")
  @AllArgsConstructor
  @NoArgsConstructor
  @EqualsAndHashCode
  public static class Initiator {

    public static final Initiator SYSTEM = Initiator.builder().id("system").displayName("system").build();

    @JacksonXmlProperty(localName = "ID")
    private String id;

    @JacksonXmlProperty(localName = "DisplayName")
    private String displayName;

  }

}
