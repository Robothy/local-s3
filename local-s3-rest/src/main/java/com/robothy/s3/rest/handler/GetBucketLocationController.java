package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.response.LocationConstraint;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.LocalS3Constants;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketLocation.html">GetBucketLocation</a>
 */
class GetBucketLocationController implements HttpRequestHandler {

  private final XmlMapper xmlMapper;

  private final BucketService bucketService;

  public GetBucketLocationController(ServiceFactory serviceFactory) {
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
    this.bucketService = serviceFactory.getInstance(BucketService.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    Bucket bucket = bucketService.getBucket(bucketName);
    LocationConstraint locationConstraint = LocationConstraint.builder()
        .locationConstraint(bucket.getRegion().orElse(LocalS3Constants.DEFAULT_LOCATION_CONSTRAINT))
        .build();
    response.write(xmlMapper.writeValueAsString(locationConstraint));
    ResponseUtils.addCommonHeaders(response);
  }

}
