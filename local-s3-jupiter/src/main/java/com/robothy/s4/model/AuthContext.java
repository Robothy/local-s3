package com.robothy.s4.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthContext {

  private String accessKey;

}
