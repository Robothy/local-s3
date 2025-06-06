package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeletePublicAccessBlock.html">DeletePublicAccessBlock</a>.
 * Removes the PublicAccessBlock configuration from a bucket.
 */
class DeletePublicAccessBlockController implements HttpRequestHandler {

  private final BucketService bucketService;

  DeletePublicAccessBlockController(ServiceFactory serviceFactory) {
    this.bucketService = serviceFactory.getInstance(BucketService.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    bucketService.deletePublicAccessBlock(bucketName);

    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }
}
