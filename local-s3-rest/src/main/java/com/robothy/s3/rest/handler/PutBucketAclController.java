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
import io.netty.buffer.ByteBufInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutBucketAcl.html">PutBucketAcl</a>.
 * LocalS3 only stores the Acl information for the specified bucket;
 * it doesn't do granting actions.
 */
class PutBucketAclController implements HttpRequestHandler {

  private final BucketAclService aclService;

  private final XmlMapper xmlMapper;

  PutBucketAclController(ServiceFactory serviceFactory) {
    this.aclService = serviceFactory.getInstance(BucketService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

//  private static final Map<String, String> HEADER_PERMISSION_MAP = Map.of(
//      "x-amz-grant-full-control", "FULL_CONTROL",
//      "x-amz-grant-read", "READ",
//      "x-amz-grant-read-acp","READ_ACP",
//      "x-amz-grant-write", "WRITE",
//      "x-amz-grant-write-acp", "WRITE_ACP"
//  );

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);

    try(InputStream in = new ByteBufInputStream(request.getBody())) {
      AccessControlPolicy acl = getAclFromHeader(request).orElse(xmlMapper
          .readValue(in, AccessControlPolicy.class));
      aclService.putBucketAcl(bucketName, acl);
    }

    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

  private Optional<AccessControlPolicy> getAclFromHeader(HttpRequest request) {
    // todo parse acl from header.
    return Optional.empty();
  }



}
