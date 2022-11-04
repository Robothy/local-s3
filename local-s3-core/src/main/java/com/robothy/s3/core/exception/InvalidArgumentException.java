package com.robothy.s3.core.exception;

import lombok.Getter;

@Getter
public class InvalidArgumentException extends LocalS3Exception {

  private final String argumentName;

  private final String argumentValue;

  public InvalidArgumentException(String argumentName, String argumentValue, String message) {
    super(S3ErrorCode.InvalidArgument, message);
    this.argumentName = argumentName;
    this.argumentValue = argumentValue;
  }

  public InvalidArgumentException(String argumentName, String argumentValue) {
    this(argumentName, argumentValue, null);
  }

}
