package com.robothy.s3.core.exception.vectors;

import lombok.Getter;

@Getter
public enum LocalS3VectorErrorType {

  NOT_FOUND("NotFoundException", 404),
  INDEX_NOT_FOUND("IndexNotFoundException", 404),
  INDEX_ALREADY_EXISTS("IndexAlreadyExistsException", 409),
  INVALID_REQUEST("InvalidRequestException", 400),
  ;

  private final String code;

  private final int status;

  LocalS3VectorErrorType(String code, int status) {
    this.code = code;
    this.status = status;
  }

}
