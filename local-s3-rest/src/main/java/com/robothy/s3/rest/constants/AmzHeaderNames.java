package com.robothy.s3.rest.constants;

/**
 * Amazon S3 headers.
 */
public class AmzHeaderNames {

  public static final String X_AMZ_REQUEST_ID = "x-amz-request-id";

  public static final String X_AMZ_VERSION_ID = "x-amz-version-id";

  public static final String X_AMZ_CONTENT_SHA256 = "x-amz-content-sha256";

  public static final String X_AMZ_DECODED_CONTENT_LENGTH = "x-amz-decoded-content-length";

  public static final String X_AMZ_BUCKET_REGION = "x-amz-bucket-region";

  public static final String X_AMZ_DELETE_MARKER = "x-amz-delete-marker";

  public static final String X_AMZ_TAGGING = "x-amz-tagging";

  public static final String X_AMZ_TAGGING_COUNT = "x-amz-tagging-count";

  public static final String X_AMZN_ERRORTYPE = "x-amzn-errortype";

  /**
   * Specifies the source object for the copy operation.
   */
  public static final String X_AMZ_COPY_SOURCE = "x-amz-copy-source";

  /**
   * Version of the copied object in the destination bucket.
   */
  public static final String X_AMZ_COPY_SOURCE_VERSION_ID = "x-amz-copy-source-version-id";

  /**
   * Specifies whether the metadata is copied from the source object or replaced with metadata
   * provided in the request. Valid values: COPY, REPLACE. Default: COPY.
   */
  public static final String X_AMZ_METADATA_DIRECTIVE = "x-amz-metadata-directive";

  /**
   * The prefix for <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html#UserMetadata">user-defined object metadata</a> keys.
   *
   */
  public static final String X_AMZ_META_PREFIX = "x-amz-meta-";
}
