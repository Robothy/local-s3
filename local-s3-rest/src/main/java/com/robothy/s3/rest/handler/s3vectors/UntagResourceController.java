package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.core.util.S3VectorsArnUtils;
import com.robothy.s3.datatypes.s3vectors.response.UntagResourceResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles DELETE /tags/{resourceArn}?tagKeys=k1&amp;tagKeys=k2 (AWS S3 Vectors UntagResource).
 * The resource ARN is the URL-encoded tail of the URL path; tag keys are repeated query parameters.
 */
public class UntagResourceController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public UntagResourceController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    // LocalS3Router treats /tags/{resourceArn} as bucket "tags" + object key;
    // the raw (still URL-encoded) ARN is the object key.
    String rawArn = request.getParams().getOrDefault("key", List.of("")).get(0);
    String resourceArn = URLDecoder.decode(rawArn, StandardCharsets.UTF_8);

    List<String> tagKeys = request.getParams().getOrDefault("tagKeys", List.of());

    String vectorBucketName = S3VectorsArnUtils.resolveBucketName(null, resourceArn);
    UntagResourceResponse untagResponse = s3VectorsService.untagResource(vectorBucketName, tagKeys);
    HttpRequestUtils.sendJsonResponse(response, untagResponse, objectMapper);
  }
}
