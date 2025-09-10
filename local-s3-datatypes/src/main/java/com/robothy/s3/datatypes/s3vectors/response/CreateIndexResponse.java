package com.robothy.s3.datatypes.s3vectors.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response for CreateIndex operation.
 * According to AWS specification, this operation returns an empty body (204 No Content).
 * This class exists only to provide response metadata access.
 */
@Data
@Builder
public class CreateIndexResponse {
  // Empty response body - AWS returns 204 No Content for CreateIndex
  // Metadata is handled by the SDK layer
}