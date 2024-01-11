package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3Endpoint;

public class LocalS3EndpointResolver extends AbstractLocalS3ParameterResolver {

  @Override
  protected String className() {
    return LocalS3Endpoint.class.getName();
  }

  @Override
  protected LocalS3Endpoint resolve(int port) {
    return new LocalS3Endpoint(port);
  }

}
