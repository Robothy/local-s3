package com.robothy.s3.core.exception;

import lombok.Getter;

@Getter
public class LocalS3InvalidArgumentException extends LocalS3Exception {

  private final String argumentName;

  private final String argumentValue;

  public LocalS3InvalidArgumentException(String argumentName, String argumentValue, String message) {
    super(S3ErrorCode.InvalidArgument, message);
    this.argumentName = argumentName;
    this.argumentValue = argumentValue;
  }

  public LocalS3InvalidArgumentException(String argumentName, String argumentValue) {
    this(argumentName, argumentValue, null);
  }

}
