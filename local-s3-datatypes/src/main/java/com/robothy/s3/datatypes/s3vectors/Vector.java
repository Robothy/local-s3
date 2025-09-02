package com.robothy.s3.datatypes.s3vectors;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a vector object in S3 Vectors following AWS S3 Vectors API specification.
 * A vector contains float32 data array and optional metadata for similarity search.
 * 
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutVectors.html">PutVectors API</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Vector {

  /**
   * The unique identifier for this vector within the index.
   */
  private String id;

  /**
   * The vector data as an array of float32 values.
   * The length must match the dimension specified in the vector index.
   */
  private float[] data;

  /**
   * Optional metadata associated with this vector.
   * Keys and values must conform to the metadata schema defined in the vector index.
   */
  private Map<String, String> metadata;

  /**
   * The creation date and time of the vector.
   */
  private LocalDateTime creationDate;

  /**
   * The last modification date and time of the vector.
   */
  private LocalDateTime lastModified;

  /**
   * The size in bytes of the vector data.
   * Calculated as data.length * 4 (since each float32 is 4 bytes).
   */
  public long getDataSize() {
    return data != null ? data.length * 4L : 0L;
  }

  /**
   * Get the dimension (number of elements) of this vector.
   * 
   * @return the number of elements in the data array, or 0 if data is null
   */
  public int getDimension() {
    return data != null ? data.length : 0;
  }

  /**
   * Validate that this vector's data is compatible with the specified dimension.
   * 
   * @param expectedDimension the expected dimension from the vector index
   * @throws IllegalArgumentException if the vector dimension doesn't match
   */
  public void validateDimension(int expectedDimension) {
    int actualDimension = getDimension();
    if (actualDimension != expectedDimension) {
      throw new IllegalArgumentException(
          String.format("Vector dimension mismatch: expected %d, got %d", expectedDimension, actualDimension));
    }
  }

  /**
   * Validate that all vector data values are finite (not NaN or infinite).
   * 
   * @throws IllegalArgumentException if any vector data value is invalid
   */
  public void validateData() {
    if (data == null) {
      throw new IllegalArgumentException("Vector data cannot be null");
    }
    if (data.length == 0) {
      throw new IllegalArgumentException("Vector data cannot be empty");
    }

    for (int i = 0; i < data.length; i++) {
      float value = data[i];
      if (!Float.isFinite(value)) {
        throw new IllegalArgumentException(
            String.format("Vector data contains invalid value at index %d: %f", i, value));
      }
    }
  }

}