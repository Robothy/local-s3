package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.robothy.s3.datatypes.converter.AmazonInstantConverter;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonRootName("GetBucketResult")
public class GetBucketResult {

  @JsonProperty("Bucket")
  private String bucket;

  @JsonProperty("PublicAccessBlockEnabled")
  private boolean publicAccessBlockEnabled;

  @JsonProperty("CreationDate")
  @JsonSerialize(converter = AmazonInstantConverter.class)
  private Instant creationDate;

}
