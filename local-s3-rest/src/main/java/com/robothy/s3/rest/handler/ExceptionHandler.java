package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.platform.utils.DigestUtil;
import com.robothy.s3.datatypes.response.Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Random;

/**
 * Handle any unhandled exceptions and construct an error response body.
 */
class ExceptionHandler implements com.robothy.netty.router.ExceptionHandler<Exception> {

  @Override
  public void handle(Exception e, HttpRequest request, HttpResponse response) {
    Error error = Error.builder()
        .code("InternalServerError")
        .message(e.getMessage())
        .requestId(DigestUtil.md5AsHex(String.valueOf(new Random().nextDouble()).getBytes()))
        .build();

    response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(XmlUtils.toXml(error));
  }

}
