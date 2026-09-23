package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Health check endpoint for Docker {@code HEALTHCHECK} directives and k8s
 * liveness/readiness probes. Responds 200 without S3 credentials and without
 * requiring any bucket to exist.
 *
 * <p>The {@code /_health} path can never collide with the {@code /{bucket}}
 * route because underscores are invalid in S3 bucket names, and the router
 * dispatches exact path matches before bucket resolution.
 *
 * @see <a href="https://github.com/Robothy/local-s3/issues/292">issue #292</a>
 */
class HealthCheckController implements HttpRequestHandler {

  static final String HEALTH_PATH = "/_health";

  @Override
  public void handle(HttpRequest request, HttpResponse response) {
    response.status(HttpResponseStatus.OK)
        .putHeader("Content-Type", "text/plain; charset=utf-8")
        .write("OK");
  }

}
