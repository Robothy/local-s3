package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.GetIndexRequest;
import com.robothy.s3.datatypes.s3vectors.response.GetIndexResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP controller for retrieving vector index details.
 * Handles GET requests to retrieve information about a specific vector index.
 */
@Slf4j
public class GetIndexController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public GetIndexController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    GetIndexRequest getRequest = parseRequest(request);
    GetIndexResponse getResponse = processRequest(getRequest);
    HttpRequestUtils.sendJsonResponse(response, getResponse, objectMapper);
  }

  private GetIndexRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequiredRequest(bodyBytes, GetIndexRequest.class, objectMapper);
  }

  private GetIndexResponse processRequest(GetIndexRequest getRequest) {
    IndexIdentifier indexIdentifier = VectorIndexAssertions.resolveIndexIdentifier(
        getRequest.getVectorBucketName(),
        getRequest.getIndexArn(),
        getRequest.getIndexName()
    );
    
    return s3VectorsService.getIndex(indexIdentifier.bucketName(), indexIdentifier.indexName());
  }

}
