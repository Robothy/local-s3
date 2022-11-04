package com.robothy.s3.core.util;

import java.io.File;

public class FileUtils {

  public static void deleteRecursively(File root) {
    if (!root.exists()) {
      return;
    }

    if (root.isFile()){
      root.delete();
    } else {
      File[] children = root.listFiles();
      if (null == children || 0 == children.length) {
        root.delete();
      } else {
        for (File file : children) {
          deleteRecursively(file);
        }
      }
    }
  }

}
