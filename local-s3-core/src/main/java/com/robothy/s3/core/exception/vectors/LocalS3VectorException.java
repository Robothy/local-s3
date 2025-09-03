package com.robothy.s3.core.exception.vectors;

import lombok.Getter;

@Getter
public class LocalS3VectorException extends RuntimeException {

  private final LocalS3VectorErrorType errorType;

  private final String message;

  public LocalS3VectorException(LocalS3VectorErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
    this.message = message;
  }

}
