package com.robothy.s3.core.util;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import lombok.SneakyThrows;

public class JsonUtils {

  private static final JsonMapper jsonMapper = new JsonMapper();

  static {
    jsonMapper.registerModule(new Jdk8Module());
  }

  private static final ObjectReader jsonReader = jsonMapper.reader();

  private static final ObjectWriter jsonWriter = jsonMapper.writer();

  private static final ObjectWriter prettyJsonWriter = jsonMapper.writer().withDefaultPrettyPrinter();

  @SneakyThrows
  public static <T> T fromJson(String json, Class<T> clazz) {
    return jsonReader.readValue(json, clazz);
  }

  @SneakyThrows
  public static <T> T fromJson(URL url, Class<T> clazz) {
    return jsonReader.readValue(url, clazz);
  }

  @SneakyThrows
  public static <T> T fromJson(File jsonFile, Class<T> clazz) {
    return jsonReader.readValue(jsonFile, clazz);
  }

  @SneakyThrows
  public static <T> T fromJson(InputStream jsonInputStream, Class<T> clazz) {
    return jsonReader.readValue(jsonInputStream, clazz);
  }

  @SneakyThrows
  public static String toJson(Object object) {
    return jsonWriter.writeValueAsString(object);
  }

  @SneakyThrows
  public static void toJson(File destFile, Object object) {
    jsonWriter.writeValue(destFile, object);
  }

  @SneakyThrows
  public static void toJson(OutputStream out, Object object) {
    jsonWriter.writeValue(out, object);
  }

  @SneakyThrows
  public static String toPrettyJson(Object object) {
    return prettyJsonWriter.writeValueAsString(object);
  }

  @SneakyThrows
  public static void toPrettyJson(File destFile, Object object) {
    prettyJsonWriter.writeValue(destFile, object);
  }

  @SneakyThrows
  public static void toPrettyJson(OutputStream out, Object object) {
    prettyJsonWriter.writeValue(out, object);
  }

}
