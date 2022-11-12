package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class LocalS3EndpointResolver extends AbstractLocalS3ParameterResolver<LocalS3Endpoint> {

  @Override
  protected Class<LocalS3Endpoint> type() {
    return LocalS3Endpoint.class;
  }

  @Override
  protected LocalS3Endpoint resolve(int port) {
    return new LocalS3Endpoint(port);
  }

}
