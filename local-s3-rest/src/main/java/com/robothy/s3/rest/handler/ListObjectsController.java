package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.service.ListObjectsService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.model.response.CommonPrefix;
import com.robothy.s3.rest.model.response.ListBucketResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.stream.Collectors;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjects.html">ListObjects</a>.
 */
class ListObjectsController implements HttpRequestHandler {

  private final ListObjectsService listObjectsService;

  private final XmlMapper xmlMapper;

  public ListObjectsController(ServiceFactory serviceFactory) {
    this.listObjectsService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String delimiter = RequestAssertions.assertDelimiterIsValid(request).orElse(null);
    String encodingType = RequestAssertions.assertEncodingTypeIsValid(request).orElse(null);
    String marker = request.parameter("marker").orElse(null);
    int maxKeys = Math.min(1000, request.parameter("max-keys").map(Integer::valueOf).orElse(1000));
    String prefix = request.parameter("prefix").orElse(null);

    ListObjectsAns listObjectsAns = listObjectsService.listObjects(bucket, delimiter, encodingType, marker, maxKeys, prefix);

    ListBucketResult listBucketResult = ListBucketResult.builder()
        .isTruncated(listObjectsAns.isTruncated())
        .marker(listObjectsAns.getMarker())
        .nextMarker(listObjectsAns.getNextMarker().orElse(null))
        .contents(listObjectsAns.getObjects())
        .name(bucket)
        .prefix(listObjectsAns.getPrefix())
        .delimiter(listObjectsAns.getDelimiter())
        .maxKeys(listObjectsAns.getMaxKeys())
        .commonPrefixes(listObjectsAns.getCommonPrefixes().stream().map(CommonPrefix::new).collect(Collectors.toList()))
        .encodingType(listObjectsAns.getEncodingType())
        .build();

    String xml = xmlMapper.writeValueAsString(listBucketResult);
    response.status(HttpResponseStatus.OK)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), xml.length())
        .write(xml);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addDateHeader(response);
  }

}
