package com.robothy.s3.core.model.internal.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.Test;

class VectorIndexMetadataMapConverterTest {

  @Test
  void convert_withNullValue_returnsEmptyMap() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(null);
    
    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertInstanceOf(ConcurrentSkipListMap.class, result);
  }

  @Test
  void convert_withEmptyMap_returnsSameMap() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    assertSame(input, result);
    assertTrue(result.isEmpty());
  }

  @Test
  void convert_withPopulatedMap_returnsSameMap() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    VectorIndexMetadata metadata1 = new VectorIndexMetadata();
    metadata1.setIndexName("index1");
    VectorIndexMetadata metadata2 = new VectorIndexMetadata();
    metadata2.setIndexName("index2");
    input.put("index1", metadata1);
    input.put("index2", metadata2);
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    assertSame(input, result);
    assertEquals(2, result.size());
    assertEquals(metadata1, result.get("index1"));
    assertEquals(metadata2, result.get("index2"));
  }

  @Test
  void convert_preservesMapType_returnsConcurrentSkipListMap() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    input.put("test", new VectorIndexMetadata());
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    assertInstanceOf(ConcurrentSkipListMap.class, result);
  }

  @Test
  void convert_preservesOrder_maintainsSortedOrder() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    
    VectorIndexMetadata metadata1 = new VectorIndexMetadata();
    metadata1.setIndexName("aaa");
    VectorIndexMetadata metadata2 = new VectorIndexMetadata();
    metadata2.setIndexName("bbb");
    VectorIndexMetadata metadata3 = new VectorIndexMetadata();
    metadata3.setIndexName("ccc");
    
    input.put("ccc", metadata3);
    input.put("aaa", metadata1);
    input.put("bbb", metadata2);
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    String[] keys = result.keySet().toArray(new String[0]);
    assertEquals("aaa", keys[0]);
    assertEquals("bbb", keys[1]);
    assertEquals("ccc", keys[2]);
  }

  @Test
  void convert_withThreadSafety_supportsConcurrentAccess() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    VectorIndexMetadata metadata = new VectorIndexMetadata();
    metadata.setIndexName("test");
    input.put("test", metadata);
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    // Verify concurrent map properties are preserved
    assertDoesNotThrow(() -> {
      result.put("concurrent-test", new VectorIndexMetadata());
      result.get("test");
      result.remove("concurrent-test");
    });
  }

  @Test
  void convert_preservesStatusValues_maintainsMetadataState() {
    VectorIndexMetadataMapConverter converter = new VectorIndexMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorIndexMetadata> input = new ConcurrentSkipListMap<>();
    
    VectorIndexMetadata activeIndex = new VectorIndexMetadata();
    activeIndex.setIndexName("active-index");
    activeIndex.setActive();
    
    VectorIndexMetadata failedIndex = new VectorIndexMetadata();
    failedIndex.setIndexName("failed-index");
    failedIndex.setFailed();
    
    input.put("active-index", activeIndex);
    input.put("failed-index", failedIndex);
    
    ConcurrentSkipListMap<String, VectorIndexMetadata> result = converter.convert(input);
    
    assertTrue(result.get("active-index").isActive());
    assertEquals("FAILED", result.get("failed-index").getStatus());
  }
}
