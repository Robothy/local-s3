package com.robothy.s3.datatypes.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("CreateBucketConfiguration ")
public class CreateBucketConfiguration {

  @JsonProperty("LocationConstraint")
  private String locationConstraint;

}
