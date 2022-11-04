package com.robothy.s3.rest.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.LocalS3Exception;
import com.robothy.s3.core.exception.S3ErrorCode;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.Error;
import com.robothy.s3.rest.service.ServiceFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Optional;

/**
 * Parse the {@linkplain LocalS3Exception} to {@linkplain Error} and response to the client.
 */
class LocalS3ExceptionHandler implements ExceptionHandler<LocalS3Exception> {

  private final XmlMapper xmlMapper = ServiceFactory.getInstance(XmlMapper.class);

  @Override
  public void handle(LocalS3Exception e, HttpRequest request, HttpResponse response) {
    S3ErrorCode s3ErrorCode = e.getS3ErrorCode();
    Error error = Error.builder()
        .code(s3ErrorCode.code())
        .message(Optional.ofNullable(e.getMessage()).orElse(s3ErrorCode.description()))
        .requestId(IdUtils.defaultGenerator().nextStrId())
        .build();

    try {
      response.status(HttpResponseStatus.valueOf(s3ErrorCode.httpStatus()))
          .write(xmlMapper.writeValueAsString(error));
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
