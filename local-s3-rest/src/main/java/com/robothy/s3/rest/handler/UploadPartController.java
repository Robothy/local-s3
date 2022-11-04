package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.request.UploadPartOptions;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.UploadPartService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.constants.AmzHeaderValues;
import com.robothy.s3.rest.model.request.DecodedAmzRequestBody;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.InputStreamUtils;
import com.robothy.s3.rest.utils.RequestUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_UploadPart.html">UploadPart</a>
 */
@Slf4j
class UploadPartController implements HttpRequestHandler {

  private final UploadPartService uploadPartService = ServiceFactory.getInstance(ObjectService.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    int partNumber = RequestAssertions.assertPartNumberIsValid(request);
    String uploadId = RequestAssertions.assertUploadIdIsProvided(request);

    DecodedAmzRequestBody decodedBody = RequestUtils.getBody(request);
    uploadPartService.uploadPart(bucket, key, uploadId, partNumber, UploadPartOptions.builder()
        .contentLength(decodedBody.getDecodedContentLength())
        .data(decodedBody.getDecodedBody())
        .build());

    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addDateHeader(response);
  }

}
