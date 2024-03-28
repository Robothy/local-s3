package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.request.CreateMultipartUploadOptions;
import com.robothy.s3.core.service.CreateMultipartUploadService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.model.response.InitiateMultipartUploadResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.RequestUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateMultipartUpload.html">CreateMultipartUpload</a>
 */
class CreateMultipartUploadController implements HttpRequestHandler {

  private final CreateMultipartUploadService uploadService;

  private final XmlMapper xmlMapper;

  CreateMultipartUploadController(ServiceFactory serviceFactory) {
    this.uploadService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    String contentType = request.header("content-type").orElse("octet/stream");
    String uploadId = uploadService.createMultipartUpload(bucket, key, CreateMultipartUploadOptions.builder()
        .tagging(RequestUtils.extractTagging(request).orElse(null))
        .contentType(contentType).build());
    InitiateMultipartUploadResult result = InitiateMultipartUploadResult.builder()
        .bucket(bucket)
        .key(key)
        .uploadId(uploadId)
        .build();
    response.status(HttpResponseStatus.OK)
        .write(xmlMapper.writeValueAsString(result));
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

}
