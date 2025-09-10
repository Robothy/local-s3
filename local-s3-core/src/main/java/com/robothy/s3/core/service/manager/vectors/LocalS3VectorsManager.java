package com.robothy.s3.core.service.manager.vectors;

import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import java.nio.file.Path;

public interface LocalS3VectorsManager {

  static LocalS3VectorsManager createInMemory() {
    return new InMemoryLocalS3VectorsManager();
  }

  static LocalS3VectorsManager createFileSystem(Path s3VectorsDataDirectory) {
    return new FileSystemLocalS3VectorsManager(s3VectorsDataDirectory);
  }

  S3VectorsService s3VectorsService();

}
