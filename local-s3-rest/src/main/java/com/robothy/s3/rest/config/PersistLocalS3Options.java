package com.robothy.s3.rest.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersistLocalS3Options {

  private int port;

  private String dataPath;

  private int nettyParentEventGroupThreadNum;

  private int nettyChildEventGroupThreadNum;

  private int s3ExecutorThreadNum;

}
