package com.robothy.s3.datatypes.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "DeleteResult")
@JsonSerialize(using = DeleteResult.DeleteResultSerializer.class)
public class DeleteResult {

  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Object> deletedList;

  @Setter
  @Getter
  @JacksonXmlRootElement(localName = "Deleted")
  public static class Deleted {

    @JacksonXmlProperty(localName = "DeleteMarker")
    private boolean deleteMarker;

    @JacksonXmlProperty(localName = "DeleteMarkerVersionId")
    private String deleteMarkerVersionId;

    @JacksonXmlProperty(localName = "Key")
    private String key;

    @JacksonXmlProperty(localName = "VersionId")
    private String versionId;

  }

  static class DeleteResultSerializer extends StdSerializer<DeleteResult> {

    DeleteResultSerializer() {
      this(null);
    }

    protected DeleteResultSerializer(Class<DeleteResult> t) {
      super(t);
    }

    @SneakyThrows
    @Override
    public void serialize(DeleteResult deleteResult, JsonGenerator gen, SerializerProvider provider) throws IOException {

      if (gen instanceof ToXmlGenerator) {
        gen.writeStartObject();
        Field deletedListField = DeleteResult.class.getDeclaredField("deletedList");
        deletedListField.setAccessible(true);
        for (Object item : (List)deletedListField.get(deleteResult)) {
          JacksonXmlRootElement jacksonXmlRootElement = item.getClass().getDeclaredAnnotation(JacksonXmlRootElement.class);
          Objects.requireNonNull(jacksonXmlRootElement, "Must add @JacksonXmlRootElement to " + item.getClass());
          gen.writeFieldName(jacksonXmlRootElement.localName());
          gen.writeObject(item);
        }

        gen.writeEndObject();
      }

    }

  }

}
