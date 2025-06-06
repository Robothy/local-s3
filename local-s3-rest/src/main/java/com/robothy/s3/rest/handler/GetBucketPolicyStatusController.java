package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.PolicyStatus;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import com.robothy.s3.rest.utils.ResponseUtils;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketPolicyStatus.html">GetBucketPolicyStatus</a>.
 * Returns the policy status for a specified bucket.
 */
class GetBucketPolicyStatusController implements HttpRequestHandler {

  private final BucketService bucketService;

  private final XmlMapper xmlMapper;

  GetBucketPolicyStatusController(ServiceFactory serviceFactory) {
    this.bucketService = serviceFactory.getInstance(BucketService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);    
    PolicyStatus policyStatus;
    try {
      policyStatus = bucketService.getBucketPolicyStatus(bucketName);
    } catch (BucketNotExistException e) {
      response.status(HttpResponseStatus.NOT_FOUND);
      return;
    }    response.write(xmlMapper.writeValueAsString(policyStatus));
    ResponseUtils.addCommonHeaders(response);
  }
}
