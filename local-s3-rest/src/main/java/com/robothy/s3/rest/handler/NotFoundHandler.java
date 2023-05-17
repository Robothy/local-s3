package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.S3Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Construct a response body when no handlers were found.
 */
class NotFoundHandler implements HttpRequestHandler {

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {

    S3Error error = S3Error.builder()
        .code("NotFound")
        .message("LocalS3 cannot found resource " + request.getUri())
        .requestId(IdUtils.nextUuid())
        .build();

    response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML);

    if (!HttpMethod.HEAD.equals(request.getMethod())) {
      response.write(XmlUtils.toXml(error));
    }
  }
}
