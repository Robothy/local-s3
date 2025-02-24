package com.robothy.s3.rest.utils;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.xml.stream.XMLInputFactory;
import lombok.SneakyThrows;

public class XmlUtils {

  private static final XmlMapper xmlMapper;

  static {
    XMLInputFactory input = new WstxInputFactory();
    input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    input.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    input.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    xmlMapper = new XmlMapper(new XmlFactory(input, new WstxOutputFactory()));
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    xmlMapper.registerModule(new Jdk8Module());
    xmlMapper.registerModule(new JavaTimeModule());
  }

  @SneakyThrows
  public static String toXml(Object object) {
    return xmlMapper.writeValueAsString(object);
  }

  @SneakyThrows
  public static String toPrettyXml(Object object) {
    return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
  }

  @SneakyThrows
  public static  <T> T fromXml(String xml, Class<T> clazz) {
    return xmlMapper.readValue(xml, clazz);
  }

}
