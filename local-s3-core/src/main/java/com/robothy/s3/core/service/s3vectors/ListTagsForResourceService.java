package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.util.vectors.ValidationUtils;
import com.robothy.s3.datatypes.s3vectors.response.ListTagsForResourceResponse;
import java.util.Map;

public interface ListTagsForResourceService extends S3VectorsMetadataAware {

  /**
   * Return the tags associated with the specified vector bucket.
   * An empty set is returned if the bucket has no tags.
   *
   * @param resourceArn the ARN of the vector bucket
   * @return the tags of the bucket
   */
  default ListTagsForResourceResponse listTagsForResource(String resourceArn) {
    ValidationUtils.validateNotBlank(resourceArn, "resourceArn is required");
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this,
        resourceArn);
    Map<String, String> tags = bucketMetadata.getTags();
    return ListTagsForResourceResponse.builder().tags(tags).build();
  }

}
