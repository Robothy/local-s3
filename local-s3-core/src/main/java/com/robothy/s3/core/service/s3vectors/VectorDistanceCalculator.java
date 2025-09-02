package com.robothy.s3.core.service.s3vectors;

public class VectorDistanceCalculator {

  /**
   * Primary implementation: Half of squared Euclidean distance on L2-normalized vectors
   * This matches the most likely S3 algorithm based on your analysis
   */
  double calculateEuclideanDistance(float[] vector1, float[] vector2) {
    if (vector1.length != vector2.length) {
      throw new IllegalArgumentException("Vector dimensions must match");
    }

    // Step 1: Normalize both vectors to unit length
    float[] normalizedV1 = createUnitVector(vector1);
    float[] normalizedV2 = createUnitVector(vector2);

    // Step 2: Calculate squared Euclidean distance
    double squaredSum = 0.0;
    for (int i = 0; i < normalizedV1.length; i++) {
      double difference = normalizedV1[i] - normalizedV2[i];
      squaredSum += difference * difference;
    }

    // Step 3: Return half of the squared distance
    return squaredSum * 0.5;
  }

  /**
   * Alternative approach using cosine-based calculation
   * Formula: (1 - cosine_similarity) / 2 on normalized vectors
   */
  double calculateCosineBasedDistance(float[] vector1, float[] vector2) {
    float[] normalizedV1 = createUnitVector(vector1);
    float[] normalizedV2 = createUnitVector(vector2);

    double dotProduct = 0.0;
    for (int i = 0; i < normalizedV1.length; i++) {
      dotProduct += normalizedV1[i] * normalizedV2[i];
    }

    // For normalized vectors, cosine similarity equals dot product
    return (1.0 - dotProduct) * 0.5;
  }

  /**
   * Haversine-inspired approach using angular distance
   * Formula: sin²(θ/2) where θ is angle between normalized vectors
   */
  double calculateAngularDistance(float[] vector1, float[] vector2) {
    float[] normalizedV1 = createUnitVector(vector1);
    float[] normalizedV2 = createUnitVector(vector2);

    double dotProduct = 0.0;
    for (int i = 0; i < normalizedV1.length; i++) {
      dotProduct += normalizedV1[i] * normalizedV2[i];
    }

    // Ensure dot product is within valid range for arccos
    dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));

    double angle = Math.acos(dotProduct);
    double halfAngle = angle * 0.5;
    double sinHalfAngle = Math.sin(halfAngle);

    return sinHalfAngle * sinHalfAngle;
  }

  /**
   * Helper method to create a unit vector (L2 normalization)
   */
  private float[] createUnitVector(float[] inputVector) {
    double magnitude = computeVectorMagnitude(inputVector);

    if (magnitude == 0.0) {
      throw new IllegalArgumentException("Cannot normalize zero-magnitude vector");
    }

    float[] unitVector = new float[inputVector.length];
    for (int i = 0; i < inputVector.length; i++) {
      unitVector[i] = (float) (inputVector[i] / magnitude);
    }

    return unitVector;
  }

  /**
   * Helper method to compute vector magnitude (L2 norm)
   */
  private double computeVectorMagnitude(float[] vector) {
    double sumOfSquares = 0.0;
    for (float component : vector) {
      sumOfSquares += component * component;
    }
    return Math.sqrt(sumOfSquares);
  }

  /**
   * Test method to verify against S3 results
   */
  public void verifyAgainstS3Results() {
    // Test vectors from your analysis
    float[] query = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    float[][] testVectors = {
        {1.1f, 1.2f, 1.0f, 1.2f, 1.3f},
        {2.1f, 2.2f, 2.0f, 2.2f, 2.3f},
        {3.1f, 3.2f, 3.0f, 3.2f, 3.3f}
    };

    // Expected S3 results from your analysis
    double[] s3Results = {0.003602028, 0.0016186237, 7.5495243E-4};

    System.out.println("Verification against S3 results:");
    for (int i = 0; i < testVectors.length; i++) {
      double calculated = calculateAngularDistance(query, testVectors[i]);
      double expected = s3Results[i];
      double error = Math.abs(calculated - expected);

      System.out.printf("Vector %d: Calculated=%.9f, Expected=%.9f, Error=%.2e%n",
          i + 1, calculated, expected, error);
    }
  }

  /**
   * Batch calculation method for multiple vectors
   */
  private double[] calculateDistanceBatch(float[] queryVector, float[][] targetVectors) {
    double[] distances = new double[targetVectors.length];
    for (int i = 0; i < targetVectors.length; i++) {
      distances[i] = calculateEuclideanDistance(queryVector, targetVectors[i]);
    }
    return distances;
  }
}