package com.robothy.s3.core.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LayeredStorageTest {

  static Stream<Arguments> storage() {
    return Stream.of(
        Arguments.arguments(
            Storage.createInMemory(),
            Storage.createInMemory()
        )
    );
  }

  @MethodSource("storage")
  @ParameterizedTest
  void put(Storage front, Storage back) {
    Storage layered = Storage.createLayered(front, back);
    Long id1 = layered.put("Hello".getBytes());
    assertTrue(front.isExist(id1));
    assertFalse(back.isExist(id1));
    assertTrue(layered.isExist(id1));

    Long id2 = back.put("Hello".getBytes());
    assertTrue(layered.isExist(id2));

    front.put(id2, "World".getBytes());
    assertTrue(layered.isExist(id2));
    assertTrue(front.isExist(id2));
    assertTrue(back.isExist(id2));

    assertArrayEquals(layered.getBytes(id2), front.getBytes(id2));
    assertNotEquals(layered.getBytes(id2), back.getBytes(id2));
    assertEquals("World", new String(layered.getBytes(id2)));
    assertEquals("Hello", new String(back.getBytes(id2)));

    layered.delete(id2); // only delete from the front.
    assertTrue(layered.isExist(id2));
    assertFalse(front.isExist(id2));
    assertTrue(back.isExist(id2));
  }

  @MethodSource("storage")
  @ParameterizedTest
  void putInputStream(Storage front, Storage back) throws IOException {
    Storage layered = Storage.createLayered(front, back);
    Long id1 = layered.put(new ByteArrayInputStream("Robothy".getBytes()));
    assertTrue(layered.isExist(id1));
    assertTrue(front.isExist(id1));
    assertFalse(back.isExist(id1));

    Long id2 = back.put(new ByteArrayInputStream("Hello".getBytes()));
    assertTrue(layered.isExist(id2));
    assertFalse(front.isExist(id2));
    assertTrue(back.isExist(id2));

    layered.put(id2, new ByteArrayInputStream("World".getBytes()));
    assertEquals("World", new String(layered.getInputStream(id2).readAllBytes()));
    assertEquals("World", new String(front.getInputStream(id2).readAllBytes()));
    assertEquals("Hello", new String(back.getInputStream(id2).readAllBytes()));
  }
}