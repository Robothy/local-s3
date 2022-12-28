package com.robothy.s3.datatypes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaggingTest {

  @Test
  void serialization() throws Exception {

    Tagging tagging = new Tagging();
    List<Tagging.Tag> tags1 = Arrays.asList(new Tagging.Tag("A", "a"), new Tagging.Tag("B", "b"));
    List<Tagging.Tag> tags2 = Arrays.asList(new Tagging.Tag("C", "c"), new Tagging.Tag("D", "d"));
    Tagging.TagSet tagSet1 = new Tagging.TagSet();
    tagSet1.setTags(tags1);
    Tagging.TagSet tagSet2 = new Tagging.TagSet();
    tagSet2.setTags(tags2);
    tagging.setTagSets(Arrays.asList(tagSet1, tagSet2));

    XmlMapper xmlMapper = new XmlMapper();
    String xml = xmlMapper.writeValueAsString(tagging);
    assertEquals(tagging, xmlMapper.readValue(xml, Tagging.class));
  }

  @Test
  void toCollection() {
    Tagging tagging = new Tagging();
    List<Tagging.Tag> tags1 = Arrays.asList(new Tagging.Tag("A", "a"), new Tagging.Tag("B", "b"));
    List<Tagging.Tag> tags2 = Arrays.asList(new Tagging.Tag("C", "c"), new Tagging.Tag("D", "d"));
    Tagging.TagSet tagSet1 = new Tagging.TagSet();
    tagSet1.setTags(tags1);
    Tagging.TagSet tagSet2 = new Tagging.TagSet();
    tagSet2.setTags(tags2);
    tagging.setTagSets(Arrays.asList(tagSet1, tagSet2));

    Collection<Map<String, String>> collection = tagging.toCollection();
    assertTrue(collection.contains(Map.of("A", "a", "B", "b")));
    assertTrue(collection.contains(Map.of("C", "c", "D", "d")));

    Tagging parsedTagging = Tagging.fromCollection(collection);
    assertEquals(2, parsedTagging.getTagSets().size());
    assertTrue(parsedTagging.getTagSets().contains(tagSet1));
    assertTrue(parsedTagging.getTagSets().contains(tagSet2));
  }

  @Test
  void toArray() {
    String[][] tags = new String[][]{{"K1", "V1"}, {"K2", "V2"}};
    Tagging tagging = Tagging.fromArrays(tags);
    assertEquals(1, tagging.getTagSets().size());
    Tagging.TagSet tagSet = tagging.getTagSets().iterator().next();
    assertEquals(2, tagSet.getTags().size());

    Tagging.Tag tag1 = tagSet.getTags().get(0);
    assertEquals("K1", tag1.getKey());
    assertEquals("V1", tag1.getValue());

    Tagging.Tag tag2 = tagSet.getTags().get(1);
    assertEquals("K2", tag2.getKey());
    assertEquals("V2", tag2.getValue());

    String[][] parsedTags = tagging.toArrays();
    assertEquals(2, parsedTags.length);
    assertArrayEquals(new String[]{"K1", "V1"}, parsedTags[0]);
    assertArrayEquals(new String[]{"K2", "V2"}, parsedTags[1]);
  }

}