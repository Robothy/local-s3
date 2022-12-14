package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.S3Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle any unhandled exceptions and construct an error response body.
 */
class ExceptionHandler implements com.robothy.netty.router.ExceptionHandler<Exception> {

  @Override
  public void handle(Exception e, HttpRequest request, HttpResponse response) {
    S3Error error = S3Error.builder()
        .code("InternalServerError")
        .message(e.getMessage())
        .requestId(IdUtils.nextUuid())
        .build();

    response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(XmlUtils.toXml(error));
  }

}
