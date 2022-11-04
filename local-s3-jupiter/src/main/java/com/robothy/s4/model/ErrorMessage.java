package com.robothy.s4.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("Error")
public class ErrorMessage {

  @JsonProperty("Code")
  private String code;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("Resource")
  private String resource;

  @JsonProperty("RequestId")
  private String requestId;

}
