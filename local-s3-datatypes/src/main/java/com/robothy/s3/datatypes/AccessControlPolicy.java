package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonRootName("acl")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "AccessControlPolicy")
public class AccessControlPolicy {

  @JacksonXmlElementWrapper(localName = "AccessControlList")
  @JacksonXmlProperty(localName = "Grant")
  @JsonProperty("grant")
  private List<Grant> grants;

  @JacksonXmlProperty(localName = "Owner")
  @JsonProperty("owner")
  private Owner owner;

}
