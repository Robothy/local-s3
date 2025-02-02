package com.robothy.s3.core.service;

import com.robothy.s3.core.model.internal.ObjectMetadata;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Algorithm implementation of list objects.
 */
public class ListItemUtils {

  private static final ConcurrentSkipListMap<String, ?> EMPTY_OBJECT_MAP = new ConcurrentSkipListMap<>();

  private static <T> ConcurrentSkipListMap<String, T> emptyItemMap() {
    //noinspection unchecked
    return (ConcurrentSkipListMap<String, T>) EMPTY_OBJECT_MAP;
  }

  public static <T> NavigableMap<String, T> filterByKeyMarkerAndDelimiter(NavigableMap<String, T> keyToItems, String keyMarker, String delimiter) {
    if (Objects.isNull(keyMarker)) {
      return keyToItems;
    }

    NavigableMap<String, T> filteredByMarker = keyToItems.tailMap(keyMarker, false);
    if (Objects.isNull(delimiter) || filteredByMarker.isEmpty()) {
      return filteredByMarker;
    }

    String firstKey = filteredByMarker.firstKey();
    String firstKeyCommonPrefix;
    if (!firstKey.contains(delimiter) || (firstKeyCommonPrefix = calculateCommonPrefix(firstKey, delimiter)).compareTo(keyMarker) > 0) {
      return filteredByMarker;
    }

    String fromKey = filteredByMarker.ceilingKey(firstKeyCommonPrefix + Character.MAX_VALUE);
    if (Objects.isNull(fromKey)) {
      return emptyItemMap();
    }
    return filteredByMarker.tailMap(fromKey, true);
  }



  public static <T> NavigableMap<String, T> filterByPrefix(NavigableMap<String, T> filteredByKeyMarker, String prefix) {
    if (Objects.isNull(prefix) || filteredByKeyMarker.isEmpty()) {
      return filteredByKeyMarker;
    }

    String fromKey = filteredByKeyMarker.floorKey(prefix);
    String toKey = filteredByKeyMarker.floorKey(prefix + Character.MAX_VALUE);
    if (Objects.isNull(toKey)) {
      return emptyItemMap();
    }

    if (Objects.isNull(fromKey)) {
      return filteredByKeyMarker.headMap(toKey, true);
    }

    boolean fromKeyInclusive = fromKey.startsWith(prefix);
    return filteredByKeyMarker.subMap(fromKey, fromKeyInclusive, toKey, true);
  }

  public static String calculateCommonPrefix(String key, String delimiter) {
    return key.substring(0, key.indexOf(delimiter) + delimiter.length());
  }

}
