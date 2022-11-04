package com.robothy.s3.core.service.loader;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import java.nio.file.Path;

/**
 * Load {@linkplain LocalS3Metadata} from file system.
 */
public interface FileSystemS3MetadataLoader {

  /**
   * Create a {@linkplain FileSystemS3MetadataLoader} with default implementation.
   *
   * @return a new {@linkplain FileSystemS3MetadataLoader} instance.
   */
  static FileSystemS3MetadataLoader create() {
    return new DefaultFileSystemS3MetadataLoader();
  }

  /**
   * Load {@linkplain LocalS3Metadata} from the given {@code s3Path}.
   *
   * @param s3Path that contains {@linkplain LocalS3Metadata}.
   * @return loaded {@linkplain LocalS3Metadata} instance.
   */
  LocalS3Metadata load(Path s3Path);

}
