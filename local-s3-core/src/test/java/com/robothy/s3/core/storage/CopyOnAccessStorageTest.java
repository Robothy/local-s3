package com.robothy.s3.core.storage;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class CopyOnAccessStorageTest {

  @Test
  void test() throws IOException {
    Storage base = Storage.createInMemory();
    Storage storage = Storage.createCopyOnAccess(base);
    Long id1 = base.put("Hello".getBytes());
    assertArrayEquals("Hello".getBytes(), storage.getInputStream(id1).readAllBytes());
    assertArrayEquals("Hello".getBytes(), storage.getBytes(id1));
    base.delete(id1);
    assertFalse(base.isExist(id1));
    assertTrue(storage.isExist(id1));

    Long id2 = storage.put(new ByteArrayInputStream("World".getBytes()));
    assertFalse(base.isExist(id2));
    assertTrue(storage.isExist(id2));
    assertArrayEquals("World".getBytes(), storage.getBytes(id2));

    Long id3 = base.put("Robothy".getBytes());
    assertTrue(storage.isExist(id3));
    assertArrayEquals("Robothy".getBytes(), storage.getInputStream(id3).readAllBytes());
    base.delete(id3);
    assertFalse(base.isExist(id3));
    assertTrue(storage.isExist(id3));
  }

}