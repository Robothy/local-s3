package com.robothy.s3.rest.bootstrap;

public enum LocalS3Mode {

  /**
   * Persist the data.
   */
  PERSISTENCE,

  /**
   * Store data in memory. Data will be lost after restart the service.
   */
  IN_MEMORY
}
