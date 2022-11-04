package com.robothy.s3.rest.handler;


import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketPolicyService;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.io.InputStream;

class BucketPolicyController {

  private final BucketPolicyService bucketPolicyService = ServiceFactory.getInstance(BucketService.class);

  /**
   * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketPolicy.html">GetBucketPolicy</a>
   */
  void get(HttpRequest request, HttpResponse response) {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String bucketPolicy = bucketPolicyService.getBucketPolicy(bucketName);
    response.status(HttpResponseStatus.OK)
        .write(bucketPolicy);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

  /**
   * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutBucketPolicy.html">PutBucketPolicy</a>
   */
  void put(HttpRequest request, HttpResponse response) throws IOException {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    try(InputStream in = new ByteBufInputStream(request.getBody()) ){
      bucketPolicyService.putBucketPolicy(bucketName, new String(in.readAllBytes()));
    }
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

  /**
   * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteBucketPolicy.html">DeleteBucketPolicy</a>
   */
  void delete(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(httpRequest);
    bucketPolicyService.deleteBucketPolicy(bucketName);
    httpResponse.status(HttpResponseStatus.OK);
    ResponseUtils.addDateHeader(httpResponse);
    ResponseUtils.addServerHeader(httpResponse);
    ResponseUtils.addAmzRequestId(httpResponse);
  }

}
