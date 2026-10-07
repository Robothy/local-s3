package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.TagResourceResponse;
import java.util.Map;

public interface TagResourceService extends S3VectorsMetadataAware {

  /**
   * Associate the specified tags with the specified vector bucket.
   * Provided keys are set to the given values while existing other keys are kept.
   *
   * @param resourceArn the ARN of the vector bucket
   * @param tags        tags to set, must not be empty
   * @return empty response
   */
  @BucketChanged
  default TagResourceResponse tagResource(String resourceArn, Map<String, String> tags) {
    ValidationUtils.validateNotBlank(resourceArn, "resourceArn is required");
    if (tags == null || tags.isEmpty()) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "At least one tag must be provided");
    }
    for (Map.Entry<String, String> entry : tags.entrySet()) {
      ValidationUtils.validateNotBlank(entry.getKey(), "Tag key must not be blank");
      ValidationUtils.validateNotBlank(entry.getValue(), "Tag value must not be blank");
    }

    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this,
        resourceArn);
    for (Map.Entry<String, String> entry : tags.entrySet()) {
      bucketMetadata.putTag(entry.getKey(), entry.getValue());
    }
    return TagResourceResponse.builder().build();
  }

}
