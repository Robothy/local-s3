package com.robothy.s3.core.storage;

import com.robothy.s3.core.util.PathUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

/**
 * An implementation of {@linkplain Storage} based on a local directory.
 */
class LocalFileSystemStorage implements Storage {

  private final Path directory;

  /**
   * Construct a {@linkplain LocalFileSystemStorage} instance.
   *
   * @param dataPath the path is where data stores in.
   */
  public LocalFileSystemStorage(Path dataPath) {
    Objects.requireNonNull(dataPath);
    this.directory = dataPath;
    PathUtils.createDirectoryIfNotExit(directory);
  }

  @Override
  @SneakyThrows
  public Long put(Long id, byte[] data) {
    Files.write(Paths.get(directory.toString(), String.valueOf(id)), data,
        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    return id;
  }

  @Override
  @SneakyThrows
  public Long put(Long id, InputStream data) {
    Path objectPath = Paths.get(directory.toString(), String.valueOf(id));
    try (InputStream _data = data; OutputStream out = Files.newOutputStream(objectPath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
      IOUtils.copy(_data, out);
    }
    return id;
  }

  @Override
  @SneakyThrows
  public byte[] getBytes(Long id) {
    ensureExists(id);
    return Files.readAllBytes(Paths.get(directory.toString(), String.valueOf(id)));
  }

  @Override
  @SneakyThrows
  public InputStream getInputStream(Long id) {
    ensureExists(id);
    return Files.newInputStream(Paths.get(directory.toString(), String.valueOf(id)));
  }

  @Override
  @SneakyThrows
  public Long delete(Long id) {
    ensureExists(id);
    Files.delete(Paths.get(directory.toString(), String.valueOf(id)));
    return id;
  }

  @Override
  public boolean isExist(Long id) {
    return Files.exists(Paths.get(directory.toString(), String.valueOf(id)));
  }

  private void ensureExists(Long id) {
    if (!isExist(id)) {
      throw new IllegalArgumentException("Object id='" + id + "' not exist.");
    }
  }

}
