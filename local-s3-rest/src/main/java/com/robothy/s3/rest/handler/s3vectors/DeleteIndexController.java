package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.request.DeleteIndexRequest;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP controller for deleting vector indexes.
 * Handles DELETE requests to remove vector indexes from a vector bucket.
 */
@Slf4j
public class DeleteIndexController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public DeleteIndexController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    DeleteIndexRequest deleteRequest = parseRequest(request);
    processRequest(deleteRequest);
    HttpRequestUtils.sendEmptyJsonResponse(response);
  }

  private DeleteIndexRequest parseRequest(HttpRequest request) throws Exception {
    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    return HttpRequestUtils.parseRequiredRequest(bodyBytes, DeleteIndexRequest.class, objectMapper);
  }

  private void processRequest(DeleteIndexRequest deleteRequest) {
    IndexIdentifier indexIdentifier = VectorIndexAssertions.resolveIndexIdentifier(
        deleteRequest.getVectorBucketName(),
        deleteRequest.getIndexArn(),
        deleteRequest.getIndexName()
    );
    
    s3VectorsService.deleteIndex(indexIdentifier.bucketName(), indexIdentifier.indexName());
  }
}
