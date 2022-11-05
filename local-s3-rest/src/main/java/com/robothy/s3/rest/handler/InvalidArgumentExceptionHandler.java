package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.InvalidArgumentException;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;

public class InvalidArgumentExceptionHandler implements ExceptionHandler<InvalidArgumentException> {

  @Override
  public void handle(InvalidArgumentException e, HttpRequest request, HttpResponse response) {
    Error error = Error.builder()
        .requestId(IdUtils.defaultGenerator().nextStrId())
        .code(e.getS3ErrorCode().code())
        .message(e.getMessage() == null ? e.getS3ErrorCode().description() : e.getMessage())
        .build();

    String xml = XmlUtils.toXml(error);
    response.status(HttpResponseStatus.valueOf(e.getS3ErrorCode().httpStatus()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(xml);
  }

}
