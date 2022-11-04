package com.robothy.s3.core.model.internal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.robothy.s3.core.converters.deserializer.UploadPartMetadataMapConverter;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UploadMetadata {

  private long createDate;

  private String contentType;

  @JsonDeserialize(converter = UploadPartMetadataMapConverter.class)
  @Builder.Default
  private NavigableMap<Integer, UploadPartMetadata> parts = new ConcurrentSkipListMap<>();

}
