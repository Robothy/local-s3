package com.robothy.s3.core.provider;

import com.robothy.s3.core.model.internal.LocalS3Metadata;

/**
 * Provide the {@linkplain LocalS3Metadata} instance.
 * Used to create an initialized {@linkplain LocalS3Metadata} instance.
 */
public interface S3MetadataProvider {

  /**
   * Create and initialize a {@linkplain LocalS3Metadata}.
   *
   * @return an initialized {@linkplain LocalS3Metadata}.
   */
  LocalS3Metadata get();

}
