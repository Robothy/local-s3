package com.robothy.s3.jupiter.docker;

import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When {@code @LocalS3Container} is present on a test method, the Junit5 extension starts a Docker
 * container with specified options in this annotation and injects instances like
 * {@linkplain com.amazonaws.services.s3.AmazonS3}, to the parameters of a test method.
 * For example:
 *
 * <pre>
 *   class HelloWorldTest{
 *     &#64;LocalS3Container
 *     void test(AmazonS3 s3) {
 *       s3.createBucket("my-bucket");
 *     }
 *   }
 * </pre>
 *
 * In the "after each" callback, the container will be stopped and deleted.
 *
 * <p> {@code @LocalS3Container} is similar to {@code @LocalS3}; if you don't know which to choose,
 * {@code @LocalS3} is a better choice.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface LocalS3Container {

  /**
   * The Docker image tag.
   *
   * @return Docker image tag.
   */
  String tag();

  /**
   * The host path bind to the container path "/data", which stores all
   * data of LocalS3 if it is started in {@linkplain LocalS3Mode#PERSISTENCE} mode.
   *
   * @return host data path.
   */
  String dataPath() default "";

  /**
   * The host TCP port that provides LocalS3 service.
   *
   * @return host TCP port.
   */
  int port() default -1;

  /**
   * LocalS3 running mode.
   *
   * @return LocalS3 running mode.
   */
  LocalS3Mode mode() default LocalS3Mode.IN_MEMORY;

}
