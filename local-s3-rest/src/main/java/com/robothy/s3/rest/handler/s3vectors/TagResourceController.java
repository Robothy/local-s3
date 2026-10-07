package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.core.util.S3VectorsArnUtils;
import com.robothy.s3.datatypes.s3vectors.request.TagResourceRequest;
import com.robothy.s3.datatypes.s3vectors.response.TagResourceResponse;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.HttpRequestUtils;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles POST /tags/{resourceArn} (AWS S3 Vectors TagResource).
 * The resource ARN is the URL-encoded tail of the URL path; the tags map is the JSON body.
 */
public class TagResourceController implements HttpRequestHandler {

  private final S3VectorsService s3VectorsService;
  private final ObjectMapper objectMapper;

  public TagResourceController(ServiceFactory serviceFactory) {
    this.s3VectorsService = serviceFactory.getInstance(S3VectorsService.class);
    this.objectMapper = serviceFactory.getInstance(ObjectMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    // LocalS3Router treats /tags/{resourceArn} as bucket "tags" + object key;
    // the raw (still URL-encoded) ARN is the object key.
    String rawArn = request.getParams().getOrDefault("key", java.util.List.of("")).get(0);
    String resourceArn = URLDecoder.decode(rawArn, StandardCharsets.UTF_8);

    byte[] bodyBytes = HttpRequestUtils.extractRequestBody(request);
    Map<String, String> tags = new HashMap<>();
    if (bodyBytes.length > 0 && !new String(bodyBytes, StandardCharsets.UTF_8).trim().isEmpty()) {
      TagResourceRequest tagRequest =
          objectMapper.readValue(bodyBytes, TagResourceRequest.class);
      if (tagRequest.getTags() != null) {
        tags = tagRequest.getTags();
      }
    }

    String vectorBucketName = S3VectorsArnUtils.resolveBucketName(null, resourceArn);
    TagResourceResponse tagResponse = s3VectorsService.tagResource(vectorBucketName, tags);
    HttpRequestUtils.sendJsonResponse(response, tagResponse, objectMapper);
  }
}
