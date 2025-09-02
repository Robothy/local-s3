package io.github.robothy.s3;

import io.github.robothy.s3.extensions.RealS3Extension;
import io.github.robothy.s3.extensions.RealS3ClientResolver;
import io.github.robothy.s3.extensions.RealS3VectorsClientResolver;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @RealS3} is a JUnit5/Jupiter extension that configures test methods to use real AWS S3 services.
 * This annotation allows injection of configured S3 clients that connect to actual AWS S3 endpoints.
 * 
 * <p>You can inject the following parameter types in test methods:
 * <ul>
 *   <li>{@linkplain software.amazon.awssdk.services.s3.S3Client}</li>
 *   <li>{@linkplain software.amazon.awssdk.services.s3vectors.S3VectorsClient}</li>
 * </ul>
 *
 * <p>Example usage with S3Client injection:
 * <pre>{@code
 *  class RealS3Test {
 *    @Test
 *    @RealS3(region = "us-west-2")
 *    void testWithRealS3(S3Client s3) {
 *      s3.createBucket(CreateBucketRequest.builder().bucket("my-test-bucket").build());
 *    }
 *  }
 * }</pre>
 *
 * <p>Example usage with S3VectorsClient injection:
 * <pre>{@code
 *  class RealS3VectorsTest {
 *    @Test
 *    @RealS3(region = "us-east-1", accessKey = "AKIA...", secretKey = "secret...")
 *    void testVectorOperations(S3VectorsClient s3VectorsClient) {
 *      CreateVectorBucketResponse response = s3VectorsClient.createVectorBucket(
 *        CreateVectorBucketRequest.builder().vectorBucketName("test-vectors").build());
 *    }
 *  }
 * }</pre>
 *
 * <p>By default, the annotation loads AWS credentials from environment variables:
 * <ul>
 *   <li>{@code AWS_ACCESS_KEY_ID}</li>
 *   <li>{@code AWS_SECRET_ACCESS_KEY}</li>
 *   <li>{@code AWS_SESSION_TOKEN} (optional, for temporary credentials)</li>
 * </ul>
 *
 * <p>You can also override credentials directly in the annotation for testing purposes,
 * though environment variables are recommended for security.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RealS3Extension.class)
@ExtendWith(RealS3ClientResolver.class)
@ExtendWith(RealS3VectorsClientResolver.class)
public @interface RealS3 {

  /**
   * AWS region for S3 operations.
   *
   * @return the AWS region (e.g., "us-east-1", "us-west-2")
   */
  String region() default "us-east-1";

  /**
   * AWS access key ID. If not specified, will be loaded from environment variable AWS_ACCESS_KEY_ID.
   *
   * @return the AWS access key ID
   */
  String accessKey() default "";

  /**
   * AWS secret access key. If not specified, will be loaded from environment variable AWS_SECRET_ACCESS_KEY.
   *
   * @return the AWS secret access key
   */
  String secretKey() default "";

  /**
   * AWS session token for temporary credentials. If not specified, will be loaded from environment variable AWS_SESSION_TOKEN.
   * This is optional and only needed when using temporary credentials.
   *
   * @return the AWS session token
   */
  String sessionToken() default "";

  /**
   * Custom S3 endpoint URL. Useful for testing with S3-compatible services.
   * If not specified, uses the default AWS S3 endpoint for the region.
   *
   * @return the custom endpoint URL
   */
  String endpointUrl() default "";

  /**
   * Whether to use path-style access instead of virtual-hosted-style.
   * Default is false (uses virtual-hosted-style).
   * Note: This setting only applies to S3Client, not S3VectorsClient.
   *
   * @return true to use path-style access
   */
  boolean pathStyleAccess() default false;
}
