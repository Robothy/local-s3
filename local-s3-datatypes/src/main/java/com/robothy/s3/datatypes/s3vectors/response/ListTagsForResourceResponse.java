package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ListTagsForResource operation.
 * If the action is successful, the service sends back an HTTP 200 response with the tags
 * associated with the resource.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_ListTagsForResource.html">ListTagsForResource API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListTagsForResourceResponse {

  /**
   * The tags associated with the resource.
   * Type: String to String map
   */
  @JsonProperty("tags")
  private Map<String, String> tags;

}
