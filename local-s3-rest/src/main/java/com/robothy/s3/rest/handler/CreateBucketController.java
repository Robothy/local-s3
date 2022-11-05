package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.platform.utils.DigestUtil;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.request.CreateBucketConfiguration;
import com.robothy.s3.datatypes.response.CreateBucketResult;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateBucket.html">CreateBucket</a>.
 */
@Slf4j
class CreateBucketController implements HttpRequestHandler {

  BucketService bucketService = ServiceFactory.getInstance(BucketService.class);

  XmlMapper xmlMapper = ServiceFactory.getInstance(XmlMapper.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    InputStream inputStream = new ByteBufInputStream(request.getBody());

    if (request.getBody().readableBytes() != 0) {
      CreateBucketConfiguration createBucketConfig = xmlMapper.readValue(inputStream, CreateBucketConfiguration.class);
      log.warn("The region '{}' for bucket creation is ignored.", createBucketConfig.getLocationConstraint());
    }

    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    bucketService.createBucket(bucketName);
    CreateBucketResult createBucketResult = CreateBucketResult.builder()
        .bucketArn(DigestUtil.md5AsHex(bucketName.getBytes()))
        .build();
    response.putHeader("Location", "local")
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(xmlMapper.writeValueAsString(createBucketResult));
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

}
