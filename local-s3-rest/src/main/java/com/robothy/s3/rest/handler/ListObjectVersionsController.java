package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.ListObjectVersionsAns;
import com.robothy.s3.core.service.ListObjectVersionsService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.datatypes.response.DeleteMarkerEntry;
import com.robothy.s3.datatypes.response.ObjectVersion;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.model.response.CommonPrefix;
import com.robothy.s3.rest.model.response.ListVersionsResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectVersions.html">ListObjectVersions</a>
 */
class ListObjectVersionsController implements HttpRequestHandler {

  private final ListObjectVersionsService listObjectVersionsService;

  private final XmlMapper xmlMapper;

  ListObjectVersionsController(ServiceFactory serviceFactory) {
    this.listObjectVersionsService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String delimiter = RequestAssertions.assertDelimiterIsValid(request).orElse(null);
    String encodingType = RequestAssertions.assertEncodingTypeIsValid(request).orElse(null);
    String keyMarker = request.parameter("key-marker").orElse(null);
    int maxKeys = Math.min(1000, request.parameter("max-keys").map(Integer::parseInt).orElse(1000));
    String prefix = request.parameter("prefix").orElse(null);
    String versionIdMarker = request.parameter("version-id-marker").orElse(null);

    ListObjectVersionsAns ans =
        listObjectVersionsService.listObjectVersions(bucketName, delimiter, keyMarker, maxKeys, prefix, versionIdMarker);

    ListVersionsResult result = ListVersionsResult.builder()
        .isTruncated(ans.getNextKeyMarker().isPresent())
        .keyMarker(keyMarker)
        .versionIdMarker(versionIdMarker)
        .nextKeyMarker(ans.getNextKeyMarker().orElse(null))
        .nextVersionIdMarker(ans.getNextVersionIdMarker().orElse(null))
        .versions(ans.getVersions())
        .name(bucketName)
        .prefix(prefix)
        .delimiter(delimiter)
        .maxKeys(maxKeys)
        .commonPrefixes(ans.getCommonPrefixes().stream().map(CommonPrefix::new).collect(Collectors.toList()))
        .encodingType(encodingType)
        .build();

    if ("url".equalsIgnoreCase(encodingType)) {
      result.getVersions().forEach(versionItem -> {
        if (versionItem instanceof ObjectVersion) {
          ObjectVersion objectVersion = (ObjectVersion) versionItem;
          objectVersion.setKey(URLEncoder.encode(objectVersion.getKey(), StandardCharsets.UTF_8));
        } else if (versionItem instanceof DeleteMarkerEntry) {
          DeleteMarkerEntry deleteMarker = (DeleteMarkerEntry) versionItem;
          deleteMarker.setKey(URLEncoder.encode(deleteMarker.getKey(), StandardCharsets.UTF_8));
        }
      });
    }

    response.status(HttpResponseStatus.OK)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(xmlMapper.writeValueAsString(result));
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

}
