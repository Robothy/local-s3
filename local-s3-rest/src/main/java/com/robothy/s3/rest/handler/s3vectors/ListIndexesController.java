package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.core.util.S3VectorsArnUtils;
import com.robothy.s3.datatypes.s3vectors.request.ListIndexesRequest;
import com.robothy.s3.datatypes.s3vectors.response.ListIndexesResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP controller for listing vector indexes within a vector bucket.
 * Handles POST requests to list all vector indexes in the specified bucket.
 */
@Slf4j
public class ListIndexesController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public ListIndexesController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    ListIndexesRequest listRequest = parseRequest(request);
    ListIndexesResponse listResponse = processRequest(listRequest);
    HttpRequestUtils.sendJsonResponse(response, listResponse, objectMapper);
  }

  private ListIndexesRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequestOrDefault(bodyBytes, ListIndexesRequest.class, objectMapper);
  }

  private ListIndexesResponse processRequest(ListIndexesRequest listRequest) {
    String vectorBucketName = S3VectorsArnUtils.resolveBucketName(
        listRequest.getVectorBucketName(), 
        listRequest.getVectorBucketArn()
    );
    
    return s3VectorsService.listIndexes(
        vectorBucketName,
        listRequest.getMaxResults(),
        listRequest.getNextToken(),
        listRequest.getPrefix()
    );
  }

}
