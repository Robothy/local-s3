package com.robothy.s3.core.util;

import java.io.File;
import java.nio.file.Path;

public class PathUtils {

  public static void createDirectoryIfNotExit(Path path) {
    File directory = path.toFile();
    if (!directory.exists() || !directory.isDirectory()) {
      if (!directory.mkdirs()) {
        throw new IllegalStateException("Cannot create directory " + path.toAbsolutePath());
      }
    }
  }

}
