package com.robothy.s3.datatypes.s3vectors;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Distance metrics supported by S3 Vectors for vector similarity calculations.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_S3_Vectors.html">AWS S3 Vectors API</a>
 */
public enum DistanceMetric {
  
  /**
   * Euclidean distance metric: √(∑(ai - bi)²)
   * Measures the straight-line distance between two points in multi-dimensional space.
   */
  EUCLIDEAN("euclidean"),
  
  /**
   * Cosine distance metric: 1 - (A·B)/(||A|| × ||B||)
   * Measures the cosine of the angle between two vectors, normalized to [0,2].
   */
  COSINE("cosine");

  private final String value;

  DistanceMetric(String value) {
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
