package com.robothy.s3.datatypes.s3vectors;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Data types supported for vector storage in S3 Vectors.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_S3_Vectors.html">AWS S3 Vectors API</a>
 */
public enum VectorDataType {
  
  /**
   * 32-bit floating point data type.
   * Each vector element is stored as a 4-byte IEEE 754 single-precision floating point number.
   */
  FLOAT32("float32");

  private final String value;

  VectorDataType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
