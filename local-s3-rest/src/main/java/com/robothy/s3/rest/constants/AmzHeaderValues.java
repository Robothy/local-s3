package com.robothy.s3.rest.constants;

/**
 * Amazon S3 common header values.
 */
public class AmzHeaderValues {

  /**
   * Value of {@linkplain AmzHeaderNames#X_AMZ_CONTENT_SHA256}. This value means
   * the payload are encoded with signatures.
   * <p>
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-auth-using-authorization-header.html">Streaming Signature Version 4</a>
   */
  public static final String STREAMING_AWS4_HMAC_SHA_256_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";

  public static final String STREAMING_AWS4_HMAC_SHA256_PAYLOAD_TRAILER = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD-TRAILER";

  public static final String STREAMING_UNSIGNED_PAYLOAD_TRAILER = "STREAMING-UNSIGNED-PAYLOAD-TRAILER";

  public static final String STREAMING_UNSIGNED_PAYLOAD = "STREAMING-UNSIGNED-PAYLOAD";

  public static final String STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD = "STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD";

  public static final String STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD_TRAILER = "STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD-TRAILER";

}
