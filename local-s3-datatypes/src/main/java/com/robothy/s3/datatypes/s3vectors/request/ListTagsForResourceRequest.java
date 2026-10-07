package com.robothy.s3.datatypes.s3vectors.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for ListTagsForResource operation.
 * <p>Returns the tags that are associated with the specified vector bucket or vector index.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListTagsForResource.html">ListTagsForResource API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListTagsForResourceRequest {

  /**
   * The Amazon Resource Name (ARN) of the resource to list tags for.
   * Type: String
   * Required: Yes
   */
  @JsonProperty("resourceArn")
  private String resourceArn;

}
