package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.platform.utils.DigestUtil;
import com.robothy.s3.datatypes.response.Error;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Random;

/**
 * Construct a response body when no handlers were found.
 */
class NotFoundHandler implements HttpRequestHandler {

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {

    Error error = Error.builder()
        .code("NotFound")
        .message("LocalS3 cannot found resource " + request.getUri())
        .requestId(DigestUtil.md5AsHex(String.valueOf(new Random().nextDouble()).getBytes()))
        .build();

    response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        .write(XmlUtils.toXml(error));
  }
}
