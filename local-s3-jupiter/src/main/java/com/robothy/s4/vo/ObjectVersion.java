package com.robothy.s4.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonRootName("Version")
@Getter
@Setter
@Builder
public class ObjectVersion {

  @JsonProperty("Key")
  private String key;

  @JsonProperty("VersionId")
  private String versionId;

  @JsonProperty("Size")
  private long size;

}
