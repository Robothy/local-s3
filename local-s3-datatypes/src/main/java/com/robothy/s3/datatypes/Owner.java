package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Owner {

  public static Owner DEFAULT_OWNER = new Owner("LocalS3", "001");

  @JsonProperty("DisplayName")
  private String displayName;

  @JsonProperty("ID")
  private String id;

}
