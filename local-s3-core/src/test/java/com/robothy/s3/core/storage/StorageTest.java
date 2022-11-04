package com.robothy.s3.core.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StorageTest {

  @Test
  void testCreate() throws IOException {
    Path storage = Files.createTempDirectory("storage");
    storage.toFile().deleteOnExit();
    assertInstanceOf(InMemoryStorage.class, Storage.create(StorageOptions.builder().build()));
    assertInstanceOf(LocalFileSystemStorage.class,
        Storage.create(StorageOptions.builder().directory(storage).inMemory(false).build()));
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void test(Storage storage) throws IOException {
    Long helloId = storage.put("Hello".getBytes());
    assertArrayEquals("Hello".getBytes(), storage.getBytes(helloId));
    assertEquals(helloId, storage.put(helloId, "Hi".getBytes()));
    assertArrayEquals("Hi".getBytes(), storage.getBytes(helloId));

    try(InputStream in = storage.getInputStream(helloId)) {
      assertArrayEquals("Hi".getBytes(), in.readAllBytes());
    }

    Long hiId = storage.put(new ByteArrayInputStream("嗨嗨害".getBytes(StandardCharsets.UTF_8)));
    assertEquals("嗨嗨害", new String(storage.getBytes(hiId), StandardCharsets.UTF_8));

    assertThrows(IllegalArgumentException.class, () -> storage.getBytes(666L));
    assertThrows(IllegalArgumentException.class, () -> storage.getInputStream(666L));
    assertEquals(helloId, storage.delete(helloId));
    assertThrows(IllegalArgumentException.class, () -> storage.delete(helloId));
  }

  static Stream<Arguments> testCases() throws IOException {
    Path storage = Files.createTempDirectory("storage");
    return Stream.of(arguments(new InMemoryStorage(StorageOptions.builder().build())),
        arguments(new LocalFileSystemStorage(StorageOptions.builder()
            .directory(storage)
            .build())));
  }

}