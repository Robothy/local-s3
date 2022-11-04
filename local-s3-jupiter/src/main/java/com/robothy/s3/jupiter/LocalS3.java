package com.robothy.s3.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LocalS3Extension.class)
@ExtendWith(AmazonS3Resolver.class)
public @interface LocalS3 {

  int port() default -1;

  boolean inmemory() default true;

}
