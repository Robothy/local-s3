package com.robothy.s3.core.service;

import com.robothy.s3.core.model.internal.LocalS3Metadata;

/**
 * Represent services that perform operations on a {@linkplain LocalS3Metadata} instance.
 */
public interface LocalS3MetadataApplicable {

  LocalS3Metadata localS3Metadata();

}
