package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Grantee.html">Grantee</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Grantee {

  @JsonProperty("DisplayName")
  private String displayName;

  @JsonProperty("EmailAddress")
  private String emailAddress;

  @JsonProperty("ID")
  private String id;

  @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
  private String xsi = "http://www.w3.org/2001/XMLSchema-instance";

  @JsonAlias("type")
  @JacksonXmlProperty(localName = "xsi:type", isAttribute = true)
  private String type;

  @JsonProperty("URI")
  private String uri;

}
