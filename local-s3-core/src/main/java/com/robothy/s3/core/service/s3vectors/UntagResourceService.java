package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.UntagResourceResponse;
import java.util.List;

public interface UntagResourceService extends S3VectorsMetadataAware {

  /**
   * Remove the tags with the specified keys from the specified vector bucket.
   * Removing a key that does not exist is not an error.
   *
   * @param resourceArn the ARN of the vector bucket
   * @param tagKeys     keys of the tags to remove, must not be empty
   * @return empty response
   */
  @BucketChanged
  default UntagResourceResponse untagResource(String resourceArn, List<String> tagKeys) {
    ValidationUtils.validateNotBlank(resourceArn, "resourceArn is required");
    ValidationUtils.validateNotNullOrEmpty(tagKeys, "At least one tag key must be provided");
    for (String tagKey : tagKeys) {
      ValidationUtils.validateNotBlank(tagKey, "Tag key must not be blank");
    }

    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this,
        resourceArn);
    for (String tagKey : tagKeys) {
      bucketMetadata.removeTag(tagKey);
    }
    return UntagResourceResponse.builder().build();
  }

}
