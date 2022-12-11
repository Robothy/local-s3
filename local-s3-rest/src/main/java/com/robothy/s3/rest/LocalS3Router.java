package com.robothy.s3.rest;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.router.AbstractRouter;
import com.robothy.netty.router.Route;
import com.robothy.netty.router.Router;

public class LocalS3Router extends AbstractRouter {

  @Override
  public Router route(Route rule) {
    throw new UnsupportedOperationException();
  }

  @Override
  public HttpRequestHandler match(HttpRequest request) {
    return null;
  }



}
