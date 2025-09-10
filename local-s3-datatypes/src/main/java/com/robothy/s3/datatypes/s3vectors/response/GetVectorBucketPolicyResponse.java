package com.robothy.s3.datatypes.s3vectors.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for GetVectorBucketPolicy operation.
 * If the action is successful, the service sends back an HTTP 200 response with the policy JSON.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_GetVectorBucketPolicy.html">GetVectorBucketPolicy API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetVectorBucketPolicyResponse {

  /**
   * The JSON that defines the policy.
   * Type: String
   */
  @JsonProperty("policy")
  private String policy;

}
