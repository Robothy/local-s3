package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.Tagging;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketTagging.html">GetBucketTagging<a/>
 */
class GetBucketTaggingController implements HttpRequestHandler {

  private final BucketService bucketService = ServiceFactory.getInstance(BucketService.class);

  private final XmlMapper xmlMapper = ServiceFactory.getInstance(XmlMapper.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {

    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    Collection<Map<String, String>> tagSets = bucketService.getTagging(bucketName);
    Tagging tagging = Tagging.fromCollection(tagSets);
    String responseBody = xmlMapper.writeValueAsString(tagging);
    response.write(responseBody);
    response.status(HttpResponseStatus.OK);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
  }

}
