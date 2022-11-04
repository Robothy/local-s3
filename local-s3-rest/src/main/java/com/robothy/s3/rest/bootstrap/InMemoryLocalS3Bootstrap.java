package com.robothy.s3.rest.bootstrap;

import java.util.Properties;

class InMemoryLocalS3Bootstrap implements LocalS3Bootstrap {

  private Properties config;

  InMemoryLocalS3Bootstrap(Properties config) {
    this.config = config;
  }

  @Override
  public void start() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }
}
