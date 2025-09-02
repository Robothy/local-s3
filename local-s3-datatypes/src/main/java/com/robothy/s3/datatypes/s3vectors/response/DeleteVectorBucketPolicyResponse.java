package com.robothy.s3.datatypes.s3vectors.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for DeleteVectorBucketPolicy operation.
 * If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_S3VectorBuckets_DeleteVectorBucketPolicy.html">DeleteVectorBucketPolicy API</a>
 */
@Data
@Builder
public class DeleteVectorBucketPolicyResponse {

  // AWS DeleteVectorBucketPolicy response is empty with HTTP 200 status, so no fields needed

}
