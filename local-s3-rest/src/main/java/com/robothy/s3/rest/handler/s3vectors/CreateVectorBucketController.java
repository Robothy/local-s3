package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.CreateVectorBucketRequest;
import com.robothy.s3.datatypes.s3vectors.response.CreateVectorBucketResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle S3 Vectors CreateVectorBucket operation.
 */
@Slf4j
public class CreateVectorBucketController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public CreateVectorBucketController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    CreateVectorBucketRequest createRequest = parseRequest(request);
    CreateVectorBucketResponse createResponse = processRequest(createRequest);
    HttpRequestUtils.sendJsonResponse(response, createResponse, objectMapper);
  }

  private CreateVectorBucketRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequiredRequest(bodyBytes, CreateVectorBucketRequest.class, objectMapper);
  }

  private CreateVectorBucketResponse processRequest(CreateVectorBucketRequest createRequest) {
    return s3VectorsService.createVectorBucket(
        createRequest.getVectorBucketName(),
        createRequest.getEncryptionConfiguration()
    );
  }

}
