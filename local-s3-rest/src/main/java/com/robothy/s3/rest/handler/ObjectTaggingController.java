package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.GetObjectTaggingAns;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.ObjectTaggingService;
import com.robothy.s3.datatypes.Tagging;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.RequestUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Objects;

class ObjectTaggingController {

  private final ObjectTaggingService objectTaggingService;

  private final XmlMapper xmlMapper;

  ObjectTaggingController(ServiceFactory serviceFactory) {
    this.objectTaggingService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  void put(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    String versionId = request.parameter("versionId").orElse(null);
    Tagging tagging = xmlMapper.readValue(RequestUtils.getBody(request).getDecodedBody(), Tagging.class);
    String returnedVersionId = objectTaggingService.putObjectTagging(bucketName, key, versionId, tagging.toArrays());

    ResponseUtils.addCommonHeaders(response)
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, returnedVersionId)
        .status(HttpResponseStatus.OK);
  }

  void get(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    String versionId = request.parameter("versionId").orElse(null);
    GetObjectTaggingAns tags = objectTaggingService.getObjectTagging(bucketName, key, versionId);
    String body = xmlMapper.writeValueAsString(Tagging.fromArrays(tags.getTagging()));

    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.OK)
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, tags.getVersionId())
        .write(body);
  }

  void delete(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    String versionId = request.parameter("versionId").orElse(null);
    objectTaggingService.deleteObjectTagging(bucketName, key, versionId);
    if (Objects.nonNull(versionId)) {
      response.putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, versionId);
    }

    ResponseUtils.addCommonHeaders(response)
      .status(HttpResponseStatus.NO_CONTENT);
  }

}
