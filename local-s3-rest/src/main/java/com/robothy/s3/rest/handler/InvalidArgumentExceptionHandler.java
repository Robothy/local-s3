package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.InvalidArgumentException;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.Error;

public class InvalidArgumentExceptionHandler implements ExceptionHandler<InvalidArgumentException> {

  @Override
  public void handle(InvalidArgumentException e, HttpRequest request, HttpResponse response) {
    Error.builder()
        .requestId(IdUtils.defaultGenerator().nextStrId())
        .code(e.getS3ErrorCode().code())
        .message(e.getMessage() == null ? e.getS3ErrorCode().description() : e.getMessage())
        .build();
  }

}
