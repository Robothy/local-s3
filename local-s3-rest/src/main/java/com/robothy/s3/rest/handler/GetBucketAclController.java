package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketAclService;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.AccessControlPolicy;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketAcl.html">GetBucketAcl</a>.
 * Get access control of a specified bucket.
 */
class GetBucketAclController implements HttpRequestHandler {

  private final BucketAclService aclService = ServiceFactory.getInstance(BucketService.class);

  private final XmlMapper xmlMapper = ServiceFactory.getInstance(XmlMapper.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    AccessControlPolicy acl = aclService.getBucketAcl(bucketName);
    response.status(HttpResponseStatus.OK)
        .write(xmlMapper.writeValueAsString(acl));
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addDateHeader(response);
  }

}
