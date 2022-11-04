package com.robothy.s3.core.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Owner {

  private String displayName;

  private String id;

}
