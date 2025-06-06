package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration that specifies the public access block settings for a bucket.
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PublicAccessBlockConfiguration.html">PublicAccessBlockConfiguration</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "PublicAccessBlockConfiguration")
public class PublicAccessBlockConfiguration {

  @JsonProperty("BlockPublicAcls")
  private Boolean blockPublicAcls;

  @JsonProperty("IgnorePublicAcls")
  private Boolean ignorePublicAcls;

  @JsonProperty("BlockPublicPolicy")
  private Boolean blockPublicPolicy;

  @JsonProperty("RestrictPublicBuckets")
  private Boolean restrictPublicBuckets;

}
