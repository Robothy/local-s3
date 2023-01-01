package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JacksonXmlRootElement(localName = "Error")
@EqualsAndHashCode
public class S3Error {

  @JacksonXmlProperty(localName = "Code")
  private String code;

  @JacksonXmlProperty(localName = "Message")
  private String message;

  @JacksonXmlProperty(localName = "RequestId")
  private String requestId;

  @JacksonXmlProperty(localName = "ArgumentName")
  private String argumentName;

  @JacksonXmlProperty(localName = "ArgumentValue")
  private String argumentValue;

  @JacksonXmlProperty(localName = "BucketName")
  private String bucketName;

  @JacksonXmlProperty(localName = "Key")
  private String key;

  @JacksonXmlProperty(localName = "VersionId")
  private String versionId;
}
