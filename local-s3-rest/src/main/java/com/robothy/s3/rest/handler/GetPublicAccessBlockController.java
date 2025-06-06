package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.exception.NoSuchPublicAccessBlockConfigurationException;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.PublicAccessBlockConfiguration;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Optional;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetPublicAccessBlock.html">GetPublicAccessBlock</a>.
 * Gets the PublicAccessBlock configuration for a bucket.
 */
class GetPublicAccessBlockController implements HttpRequestHandler {

  private final BucketService bucketService;

  private final XmlMapper xmlMapper;

  GetPublicAccessBlockController(ServiceFactory serviceFactory) {
    this.bucketService = serviceFactory.getInstance(BucketService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }
  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    Optional<PublicAccessBlockConfiguration> configuration;
    
    try {
      configuration = bucketService.getPublicAccessBlock(bucketName);
    } catch (BucketNotExistException e) {
      response.status(HttpResponseStatus.NOT_FOUND);
      return;
    }
    
    if (configuration.isEmpty()) {
      throw new NoSuchPublicAccessBlockConfigurationException(bucketName);
    }
    
    response.write(xmlMapper.writeValueAsString(configuration.get()));
    ResponseUtils.addCommonHeaders(response);
  }
}
