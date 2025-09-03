package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.QueryVectorsRequest;
import com.robothy.s3.datatypes.s3vectors.response.QueryVectorsResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryVectorsController implements HttpRequestHandler {

  private final S3VectorsService vectorsService;
  private final ObjectMapper objectMapper;

  public QueryVectorsController(ServiceFactory serviceFactory) {
    this.vectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    QueryVectorsRequest vectorsRequest = parseRequest(request);
    QueryVectorsResponse vectorsResponse = processRequest(vectorsRequest);
    HttpRequestUtils.sendJsonResponse(response, vectorsResponse, objectMapper);
  }

  private QueryVectorsRequest parseRequest(HttpRequest request) throws Exception {
    byte[] requestBody = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequiredRequest(requestBody, QueryVectorsRequest.class, objectMapper);
  }

  private QueryVectorsResponse processRequest(QueryVectorsRequest request) {
    IndexIdentifier indexIdentifier = VectorIndexAssertions.resolveIndexIdentifier(
        request.getVectorBucketName(),
        request.getIndexArn(),
        request.getIndexName()
    );

    return vectorsService.queryVectors(
        indexIdentifier.bucketName(),
        indexIdentifier.indexName(),
        request.getQueryVector(),
        request.getTopK(),
        request.getReturnDistance(),
        request.getReturnMetadata(),
        request.getFilter()
    );
  }
}
