package com.robothy.s3.rest.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;

public class XmlUtils {

  private static XmlMapper xmlMapper =
      new XmlMapper();

  @SneakyThrows
  public static String toXml(Object object) {
    return xmlMapper.writeValueAsString(object);
  }

  @SneakyThrows
  public static  <T> T fromXml(String xml, Class<T> clazz) {
    return xmlMapper.readValue(xml, clazz);
  }

}
