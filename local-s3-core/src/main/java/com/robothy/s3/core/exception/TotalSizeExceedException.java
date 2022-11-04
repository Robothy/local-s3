package com.robothy.s3.core.exception;

/**
 * The memory total size exceeded. Server side exception.
 */
public class TotalSizeExceedException extends IllegalStateException {

  public TotalSizeExceedException(long limit, long actual) {
    super("Total size is " + actual + " bytes, exceed the maximum total size " + limit + " bytes.");
  }

}
