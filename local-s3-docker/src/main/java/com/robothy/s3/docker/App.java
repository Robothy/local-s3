package com.robothy.s3.docker;

import com.robothy.s3.rest.LocalS3;
import java.nio.file.Paths;

public class App {

  public static void main(String[] args) {
    LocalS3 localS3 = LocalS3.builder()
        .port(19090)
        .dataDirectory(Paths.get("C://snb-local-s3"))
        .build();
    localS3.start();
  }

}
