package com.robothy.s3.rest.bootstrap;

import java.util.Properties;

public interface LocalS3Bootstrap {

  static LocalS3Bootstrap bootstrap(LocalS3Mode mode, Properties config) {
    return switch (mode){
      case PERSISTENCE -> new FileSystemLocalS3Bootstrap(config);
      case IN_MEMORY -> new InMemoryLocalS3Bootstrap(config);
    };
  }

  void start();

  void shutdown();

}
