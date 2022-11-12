package com.robothy.s3.core.storage;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.TotalSizeExceedException;
import org.junit.jupiter.api.Test;

class InMemoryStorageTest {

  @Test
  void test() {
    int _2KB = 2 * 1024;
    InMemoryStorage storage = new InMemoryStorage(_2KB);
    assertThrows(TotalSizeExceedException.class, () -> storage.put(new byte[_2KB + 1]));
    Long id = storage.put(new byte[_2KB]);
    assertNotNull(id);
  }

}