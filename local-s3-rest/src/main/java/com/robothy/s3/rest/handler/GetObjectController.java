package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ByteBufUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle request of <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">GetObject</a>.
 */
class GetObjectController implements HttpRequestHandler {

  private final ObjectService objectService;

  GetObjectController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);

    GetObjectOptions options = GetObjectOptions.builder()
        .versionId(request.parameter("versionId").orElse(null))
        .build();
    GetObjectAns getObjectAns = objectService.getObject(bucket, key, options);

    if (getObjectAns.isDeleteMarker()) {
      response.status(HttpResponseStatus.METHOD_NOT_ALLOWED);
      response.putHeader(HttpHeaderNames.ALLOW.toString(), HttpMethod.DELETE)
          .putHeader(AmzHeaderNames.X_AMZ_DELETE_MARKER, true);
    } else {
      ByteBuf content = ByteBufUtils.fromInputStream(getObjectAns.getContent());
      response.status(HttpResponseStatus.OK)
          .write(content)
          .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), getObjectAns.getContentType())
          .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), getObjectAns.getSize());
    }

    response.putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, getObjectAns.getVersionId());
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addServerHeader(response);
  }

}
