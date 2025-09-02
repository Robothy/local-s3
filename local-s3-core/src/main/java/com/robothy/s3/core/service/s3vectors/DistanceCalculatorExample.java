package com.robothy.s3.core.service.s3vectors;

// Example usage
public class DistanceCalculatorExample {
  public static void main(String[] args) {
    VectorDistanceCalculator calculator = new VectorDistanceCalculator();

    // Test with your specific vectors
    float[] queryVector = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    float[] testVector = {1.1f, 1.2f, 1.0f, 1.2f, 1.3f};

    double distance = calculator.calculateCosineBasedDistance(queryVector, testVector);
    System.out.println("Distance: " + distance);

    // Run verification against S3 results
    calculator.verifyAgainstS3Results();
  }
}
