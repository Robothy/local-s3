package com.robothy.s3.core.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;
import lombok.SneakyThrows;

public class JsonUtils {

  private static final JsonMapper jsonMapper = new JsonMapper();

  static {
    jsonMapper.registerModule(new Jdk8Module());
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static final ObjectReader jsonReader = jsonMapper.reader();

  private static final ObjectWriter jsonWriter = jsonMapper.writer();

  @SneakyThrows
  public static <T> T fromJson(String json, Class<T> clazz) {
    return jsonReader.readValue(json, clazz);
  }

  @SneakyThrows
  public static <T> T fromJson(File jsonFile, Class<T> clazz) {
    return jsonReader.readValue(jsonFile, clazz);
  }

  @SneakyThrows
  public static String toJson(Object object) {
    return jsonWriter.writeValueAsString(object);
  }

  @SneakyThrows
  public static void toJson(File destFile, Object object) {
    jsonWriter.writeValue(destFile, object);
  }

}
