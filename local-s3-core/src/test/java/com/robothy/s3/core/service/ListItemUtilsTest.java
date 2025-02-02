package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.Test;

class ListItemUtilsTest {

  @Test
  void filterByPrefix() {
    NavigableMap<String, ObjectMetadata>
        filtered = ListItemUtils.filterByPrefix(new ConcurrentSkipListMap<>(Map.of()), "prefix");
    assertEquals(0, filtered.size());

    ObjectMetadata object = new ObjectMetadata();
    NavigableMap<String, ObjectMetadata> filtered1 = ListItemUtils.filterByPrefix(new ConcurrentSkipListMap<>(
        Map.of("prefix", object, "prefiu", object, "prefix1", object)), "prefix");
    assertEquals(2, filtered1.size());

    NavigableMap<String, ObjectMetadata> filtered2 =
        ListItemUtils.filterByPrefix(new ConcurrentSkipListMap<>(Map.of("prefix", object)), null);
    assertEquals(1, filtered2.size());

    NavigableMap<String, ObjectMetadata> filtered3 = ListItemUtils.filterByPrefix(new ConcurrentSkipListMap<>(Map.of(
        "prefix1", object, "prefix2", object, "prefiy", object)), "prefix");
    assertEquals(2, filtered3.size());

    NavigableMap<String, ObjectMetadata> filtered4 = ListItemUtils.filterByPrefix(new ConcurrentSkipListMap<>(Map.of(
        "prefiy", object, "prefiz", object)), "prefix");
    assertEquals(0, filtered4.size());
  }

}