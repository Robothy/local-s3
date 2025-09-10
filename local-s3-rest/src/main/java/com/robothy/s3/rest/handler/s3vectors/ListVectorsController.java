package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.ListVectorsRequest;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorsResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP controller for listing vectors in an index.
 * Handles POST requests to list vectors with optional pagination and data/metadata inclusion.
 */
@Slf4j
public class ListVectorsController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public ListVectorsController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    ListVectorsRequest listRequest = parseRequest(request);
    ListVectorsResponse listResponse = processRequest(listRequest);
    HttpRequestUtils.sendJsonResponse(response, listResponse, objectMapper);
  }

  private ListVectorsRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequiredRequest(bodyBytes, ListVectorsRequest.class, objectMapper);
  }

  private ListVectorsResponse processRequest(ListVectorsRequest listRequest) {
    IndexIdentifier indexIdentifier = VectorIndexAssertions.resolveIndexIdentifier(
        listRequest.getVectorBucketName(),
        listRequest.getIndexArn(),
        listRequest.getIndexName()
    );

    return s3VectorsService.listVectors(
        indexIdentifier.bucketName(),
        indexIdentifier.indexName(),
        listRequest.getMaxResults(),
        listRequest.getNextToken(),
        listRequest.getReturnData(),
        listRequest.getReturnMetadata(),
        listRequest.getSegmentCount(),
        listRequest.getSegmentIndex()
    );
  }

}
