package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.PublicAccessBlockConfiguration;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import java.io.InputStream;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutPublicAccessBlock.html">PutPublicAccessBlock</a>.
 * Sets the PublicAccessBlock configuration for a bucket.
 */
class PutPublicAccessBlockController implements HttpRequestHandler {

  private final BucketService bucketService;
  
  private final XmlMapper xmlMapper;

  PutPublicAccessBlockController(ServiceFactory serviceFactory) {
    this.bucketService = serviceFactory.getInstance(BucketService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);

    try(InputStream in = new ByteBufInputStream(request.getBody())) {
      PublicAccessBlockConfiguration configuration = xmlMapper.readValue(in, PublicAccessBlockConfiguration.class);
      bucketService.putPublicAccessBlock(bucketName, configuration);
    }

    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }
}
