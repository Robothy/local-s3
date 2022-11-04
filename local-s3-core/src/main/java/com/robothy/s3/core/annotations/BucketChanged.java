package com.robothy.s3.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a method invocation that will change the bucket.
 * The first parameter of an annotated method must be the bucket name.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BucketChanged {

  /**
   * Operation type of the bucket.
   *
   * @return the bucket change type.
   */
  Type type() default Type.UPDATE;

  enum Type {
    CREATE, // Bucket created

    UPDATE, // Bucket changed

    DELETE  // Bucket deleted
  }

}
