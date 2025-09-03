package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.ListVectorBucketsRequest;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorBucketsResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP controller for listing vector buckets.
 * Handles POST requests to list all vector buckets owned by the authenticated sender.
 */
@Slf4j
public class ListVectorBucketsController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public ListVectorBucketsController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    ListVectorBucketsRequest listRequest = parseRequest(request);
    ListVectorBucketsResponse listResponse = processRequest(listRequest);
    HttpRequestUtils.sendJsonResponse(response, listResponse, objectMapper);
  }

  private ListVectorBucketsRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequestOrDefault(bodyBytes, ListVectorBucketsRequest.class, objectMapper);
  }

  private ListVectorBucketsResponse processRequest(ListVectorBucketsRequest listRequest) {
    return s3VectorsService.listVectorBuckets(
        listRequest.getMaxResults(),
        listRequest.getNextToken(),
        listRequest.getPrefix()
    );
  }

}
