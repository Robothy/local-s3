package com.robothy.s3.datatypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Represents <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Tagging.html">Tagging</a>.
 */
@JsonRootName("Tagging")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tagging {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("TagSet")
  private Collection<TagSet> tagSets;

  @Data
  public static class TagSet {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("Tag")
    private List<Tag> tags;
  }

  @AllArgsConstructor
  @Data
  @NoArgsConstructor
  @JsonRootName("Tag")
  public static class Tag {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
  }

  /**
   * Convert tagging instance to a {@linkplain Collection} to reduce size.
   *
   * @return a {@linkplain Collection} that contains tags.
   */
  public Collection<Map<String, String>> toCollection() {
    Collection<Map<String, String>> collection = new ArrayList<>(tagSets.size());

    if (CollectionUtils.isNotEmpty(tagSets)) {
      tagSets.forEach(tagSet -> {
        Map<String, String> tags = new HashMap<>();
        if (Objects.nonNull(tagSet.tags)) {
          tagSet.tags.forEach(tag -> tags.put(tag.getKey(), tag.getValue()));
        }
        collection.add(tags);
      });
    }
    return collection;
  }

  /**
   * Construct a {@linkplain Tagging} instance from a {@linkplain Collection}.
   *
   * @param collection collection with tags.
   * @return the created {@linkplain Tagging} instacne.
   */
  public static Tagging fromCollection(Collection<Map<String, String>> collection) {
    Objects.requireNonNull(collection);
    Tagging tagging = new Tagging();
    tagging.tagSets = new ArrayList<>(collection.size());
    collection.forEach(tagMap -> {
      TagSet tagSet = new TagSet();
      tagSet.tags = new ArrayList<>(tagMap.size());
      tagMap.forEach((k, v) -> tagSet.tags.add(new Tag(k, v)));
      tagging.tagSets.add(tagSet);
    });
    return tagging;
  }

  /**
   * Convert tagging to a 2-D string array.
   *
   * @return a (n x 2) array.
   */
  public String[][] toArrays() {
    return this.tagSets.stream().flatMap(it -> it.tags.stream()).map(tag -> new String[]{tag.key, tag.value})
        .toArray(String[][]::new);
  }

  /**
   * Construct a {@linkplain Tagging} instance from an array.
   *
   * @return a {@linkplain Tagging} instance.
   */
  public static Tagging fromArrays(String[][] tags) {
    Tagging tagging = new Tagging();
    tagging.tagSets = new ArrayList<>(1);
    TagSet tagSet = new TagSet();
    tagging.tagSets.add(tagSet);
    tagSet.tags = Stream.of(tags).map(tag -> new Tag(tag[0], tag[1]))
        .collect(Collectors.toList());
    return tagging;
  }

}
