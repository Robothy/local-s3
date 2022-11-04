package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteBucketTagging.html">DeleteBucketTagging</a>
 */
class DeleteBucketTaggingController implements HttpRequestHandler {

  private final BucketService bucketService = ServiceFactory.getInstance(BucketService.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    bucketService.deleteTagging(bucketName);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
  }

}
