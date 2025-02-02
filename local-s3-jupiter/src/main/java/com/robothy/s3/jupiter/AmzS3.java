package com.robothy.s3.jupiter;


import com.robothy.s3.jupiter.extensions.LocalS3Extension;
import com.robothy.s3.jupiter.extensions.RealS3ClientResolver;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LocalS3Extension.class)
@ExtendWith(RealS3ClientResolver.class)
public @interface AmzS3 {

  String region() default "us-east-1";

}