package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for UntagResource operation.
 * <p>Removes the specified tag keys and their associated values from the specified
 * vector bucket or vector index.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_UntagResource.html">UntagResource API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UntagResourceRequest {

  /**
   * The Amazon Resource Name (ARN) of the resource to remove tags from.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("resourceArn")
  private String resourceArn;

  /**
   * The keys of the tags to remove from the resource.
   * Type: Array of strings
   * Required: Yes
   */
  @JsonProperty("tagKeys")
  private List<String> tagKeys;

}
