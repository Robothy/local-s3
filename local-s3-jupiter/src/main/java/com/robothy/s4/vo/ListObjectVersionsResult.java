package com.robothy.s4.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@JsonRootName("ListVersionsResult")
@JacksonXmlRootElement
public class ListObjectVersionsResult {

  @JsonProperty("IsTruncated")
  private boolean isTruncated;

  @JsonProperty("KeyMarker")
  private String keyMarker;

  @JsonProperty("VersionIdMarker")
  private String versionIdMarker;

  @JsonProperty("NextKeyMarker")
  private String nextKeyMarker;

  @JsonProperty("NextVersionIdMarker")
  private String nextVersionIdMarker;


  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("Version")
  private List<ObjectVersion> versions;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("DeleteMarker")
  private List<DeleteMarker> deleteMarkers;

}
