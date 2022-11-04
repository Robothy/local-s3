package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

/**
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Grant.html">Grant</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName("Grant")
public class Grant {

  @JsonProperty("Grantee")
  private Grantee grantee;

  @JsonProperty("Permission")
  private String permission;

}
