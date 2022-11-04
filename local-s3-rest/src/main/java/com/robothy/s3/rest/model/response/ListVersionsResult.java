package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.robothy.s3.datatypes.response.VersionItem;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "ListVersionsResult")
@JsonSerialize(using = ListVersionsResult.ListVersionsResultSerializer.class)
@Getter
public class ListVersionsResult {

  @JacksonXmlProperty(localName = "IsTruncated")
  private boolean isTruncated;

  @JacksonXmlProperty(localName = "KeyMarker")
  private String keyMarker;

  @JacksonXmlProperty(localName = "VersionIdMarker")
  private String versionIdMarker;

  @JacksonXmlProperty(localName = "NextKeyMarker")
  private String nextKeyMarker;

  @JacksonXmlProperty(localName = "NextVersionIdMarker")
  private String nextVersionIdMarker;

  private List<VersionItem> versions;

  @JacksonXmlProperty(localName = "Name")
  private String name;

  @JacksonXmlProperty(localName = "Prefix")
  private String prefix;

  @JacksonXmlProperty(localName = "Delimiter")
  private Character delimiter;

  @JacksonXmlProperty(localName = "MaxKeys")
  private int maxKeys;

  @JacksonXmlProperty(localName = "CommonPrefixes")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<CommonPrefix> commonPrefixes;

  @JacksonXmlProperty(localName = "EncodingType")
  private String encodingType;

  static class ListVersionsResultSerializer extends StdSerializer<ListVersionsResult> {

    ListVersionsResultSerializer() {
      this(null);
    }

    ListVersionsResultSerializer(Class<ListVersionsResult> type) {
      super(type);
    }

    @SneakyThrows
    @Override
    public void serialize(ListVersionsResult value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      if (gen instanceof ToXmlGenerator) {
        ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
        xmlGenerator.writeStartObject();
        Field[] fields = ListVersionsResult.class.getDeclaredFields();
        for (Field field : fields) {
          java.lang.Object fieldValue = field.get(value);
          if (fieldValue instanceof List) {
            for (java.lang.Object version : (List) fieldValue) {
              JacksonXmlRootElement annotation = version.getClass().getAnnotation(JacksonXmlRootElement.class);
              Objects.requireNonNull(annotation, "Must add @JacksonXmlRootElement to " + version.getClass());
              xmlGenerator.writeFieldName(annotation.localName());
              xmlGenerator.writeObject(version);
            }
          } else {
            JacksonXmlProperty jacksonXmlProperty = field.getAnnotation(JacksonXmlProperty.class);
            Objects.requireNonNull(jacksonXmlProperty, "Must add @JacksonXmlProperty to " + value.getClass() + "#" + field.getName());
            xmlGenerator.writeFieldName(jacksonXmlProperty.localName());
            xmlGenerator.writeObject(fieldValue);
          }
        }
        xmlGenerator.writeEndObject();
      }
    }
  }

}
