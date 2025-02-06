package com.robothy.s3.rest.constants;

/**
 * Amazon S3 common header values.
 */
public class AmzHeaderValues {

  /**
   * Value of {@linkplain AmzHeaderNames#X_AMZ_CONTENT_SHA256}. This value means
   * the payload are encoded with signatures.
   */
  public static final String STREAMING_AWS4_HMAC_SHA_256_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";

  public static final String STREAMING_UNSIGNED_PAYLOAD_TRAILER = "STREAMING-UNSIGNED-PAYLOAD-TRAILER";

}
