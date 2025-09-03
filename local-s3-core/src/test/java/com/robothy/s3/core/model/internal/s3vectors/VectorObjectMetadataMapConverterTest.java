package com.robothy.s3.core.model.internal.s3vectors;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.Test;

class VectorObjectMetadataMapConverterTest {

  @Test
  void convert_withNullValue_returnsEmptyMap() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(null);
    
    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertInstanceOf(ConcurrentSkipListMap.class, result);
  }

  @Test
  void convert_withEmptyMap_returnsSameMap() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorObjectMetadata> input = new ConcurrentSkipListMap<>();
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(input);
    
    assertSame(input, result);
    assertTrue(result.isEmpty());
  }

  @Test
  void convert_withPopulatedMap_returnsSameMap() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorObjectMetadata> input = new ConcurrentSkipListMap<>();
    VectorObjectMetadata metadata1 = new VectorObjectMetadata();
    metadata1.setVectorId("vector1");
    VectorObjectMetadata metadata2 = new VectorObjectMetadata();
    metadata2.setVectorId("vector2");
    input.put("vector1", metadata1);
    input.put("vector2", metadata2);
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(input);
    
    assertSame(input, result);
    assertEquals(2, result.size());
    assertEquals(metadata1, result.get("vector1"));
    assertEquals(metadata2, result.get("vector2"));
  }

  @Test
  void convert_preservesMapType_returnsConcurrentSkipListMap() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorObjectMetadata> input = new ConcurrentSkipListMap<>();
    input.put("test", new VectorObjectMetadata());
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(input);
    
    assertInstanceOf(ConcurrentSkipListMap.class, result);
  }

  @Test
  void convert_preservesOrder_maintainsSortedOrder() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorObjectMetadata> input = new ConcurrentSkipListMap<>();
    
    VectorObjectMetadata metadata1 = new VectorObjectMetadata();
    metadata1.setVectorId("aaa");
    VectorObjectMetadata metadata2 = new VectorObjectMetadata();
    metadata2.setVectorId("bbb");
    VectorObjectMetadata metadata3 = new VectorObjectMetadata();
    metadata3.setVectorId("ccc");
    
    input.put("ccc", metadata3);
    input.put("aaa", metadata1);
    input.put("bbb", metadata2);
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(input);
    
    String[] keys = result.keySet().toArray(new String[0]);
    assertEquals("aaa", keys[0]);
    assertEquals("bbb", keys[1]);
    assertEquals("ccc", keys[2]);
  }

  @Test
  void convert_withThreadSafety_supportsConcurrentAccess() {
    VectorObjectMetadataMapConverter converter = new VectorObjectMetadataMapConverter();
    ConcurrentSkipListMap<String, VectorObjectMetadata> input = new ConcurrentSkipListMap<>();
    VectorObjectMetadata metadata = new VectorObjectMetadata();
    metadata.setVectorId("test");
    input.put("test", metadata);
    
    ConcurrentSkipListMap<String, VectorObjectMetadata> result = converter.convert(input);
    
    // Verify concurrent map properties are preserved
    assertDoesNotThrow(() -> {
      result.put("concurrent-test", new VectorObjectMetadata());
      result.get("test");
      result.remove("concurrent-test");
    });
  }
}
