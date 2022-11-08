package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.S3ErrorCode;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Random;

/**
 * {@linkplain IllegalArgumentException} handler.
 */
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException> {

  @Override
  public void handle(IllegalArgumentException e, HttpRequest httpRequest, HttpResponse response) {
    Error error = Error.builder()
        .code(S3ErrorCode.InvalidArgument.code())
        .message(e.getMessage())
        .requestId(IdUtils.nextUuid())
        .build();

    response.status(HttpResponseStatus.valueOf(S3ErrorCode.InvalidArgument.httpStatus()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(XmlUtils.toXml(error));
  }

}
