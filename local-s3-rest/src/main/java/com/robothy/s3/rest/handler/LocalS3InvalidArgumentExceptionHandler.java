package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.netty.router.ExceptionHandler;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.util.IdUtils;
import com.robothy.s3.datatypes.response.S3Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * Example Response Body:
 *
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <Error>
 *     <Code>InvalidArgument</Code>
 *     <Message>Invalid Encoding Method specified in Request</Message>
 *     <ArgumentName>encoding-type</ArgumentName>
 *     <ArgumentValue>aa</ArgumentValue>
 *     <RequestId>VGEKQFPHD810M604</RequestId>
 *     <HostId>vieD/O9bf+rcr4eiwzXGKIrgaohTsdEiK5A3KJqreJj608+WphNzw4N0qmrQJyxnH/fuza9BEbw=</HostId>
 * </Error>
 *
 *
 * }</pre>
 */
public class LocalS3InvalidArgumentExceptionHandler implements ExceptionHandler<LocalS3InvalidArgumentException> {

  @Override
  public void handle(LocalS3InvalidArgumentException e, HttpRequest request, HttpResponse response) {
    S3Error error = S3Error.builder()
        .requestId(IdUtils.defaultGenerator().nextStrId())
        .code(e.getS3ErrorCode().code())
        .message(e.getMessage() == null ? e.getS3ErrorCode().description() : e.getMessage())
        .argumentName(e.getArgumentName())
        .argumentValue(e.getArgumentValue())
        .build();

    String xml = XmlUtils.toXml(error);
    response.status(HttpResponseStatus.valueOf(e.getS3ErrorCode().httpStatus()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(xml);
  }

}
