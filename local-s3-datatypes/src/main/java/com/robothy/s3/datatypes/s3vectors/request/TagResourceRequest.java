package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for TagResource operation.
 * <p>Associates the specified tags with the specified vector bucket or vector index.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_TagResource.html">TagResource API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagResourceRequest {

  /**
   * The Amazon Resource Name (ARN) of the resource to add tags to.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("resourceArn")
  private String resourceArn;

  /**
   * The tags to add to the resource. Tags are key-value pairs.
   * Type: String to String map
   * Required: Yes
   */
  @JsonProperty("tags")
  private Map<String, String> tags;

}
