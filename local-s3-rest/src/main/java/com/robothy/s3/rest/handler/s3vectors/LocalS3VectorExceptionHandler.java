package com.robothy.s3.rest.handler.s3vectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.datatypes.s3vectors.response.S3VectorsError;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

public class LocalS3VectorExceptionHandler implements ExceptionHandler<LocalS3VectorException> {

  private final ServiceFactory serviceFactory;

  public LocalS3VectorExceptionHandler(ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @Override
  public void handle(LocalS3VectorException e, HttpRequest httpRequest, HttpResponse httpResponse) {
    httpResponse.status(HttpResponseStatus.valueOf(e.getErrorType().getStatus()))
        .putHeader(AmzHeaderNames.X_AMZN_ERRORTYPE, e.getErrorType().getCode())
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
    ObjectMapper objectMapper = this.serviceFactory.getInstance(ObjectMapper.class);
    try {
      httpResponse.write(objectMapper.writer()
          .writeValueAsString(S3VectorsError.builder().message(e.getMessage()).build()));
    } catch (JsonProcessingException ex) {
      httpResponse.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      httpResponse.write("Internal Server Error");
    }
  }
}
