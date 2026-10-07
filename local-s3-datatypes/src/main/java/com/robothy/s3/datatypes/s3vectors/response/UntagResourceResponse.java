package com.robothy.s3.datatypes.s3vectors.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for UntagResource operation.
 * If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_UntagResource.html">UntagResource API</a>
 */
@Data
@Builder
public class UntagResourceResponse {

  // AWS UntagResource response is empty with HTTP 200 status, so no fields needed

}
